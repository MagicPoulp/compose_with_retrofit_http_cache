package com.example.testcomposethierry.ui.view_models


import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.filter
import androidx.paging.map
import com.example.testcomposethierry.BuildConfig
import com.example.testcomposethierry.data.config.AppConfig
import com.example.testcomposethierry.data.custom_structures.ResultOf
import com.example.testcomposethierry.data.models.DataArtDetail
import com.example.testcomposethierry.data.models.DataArtElement
import com.example.testcomposethierry.data.repositories.ArtDataRepository
import com.example.testcomposethierry.data.repositories.ArtDataPagingSource
import com.example.testcomposethierry.data.repositories.PersistentDataManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState {
    object Empty : UiState()
    object InProgress : UiState()
    class Error(val error: Throwable) : UiState()
    object Filled : UiState()
}

/**
 * Explanation for how the DetailScreen works with data from a separate API.
 * We parallelized the use of the APIs to increase the performance for the end user.
 * And when data is not ready, we show a Loading animation for a short time.
 *
 * There are 2 APIs, one with Global data, and one with detailed data
 * Detail data is prefetched when the general data emits to a Channel
 * See processInParallelToGetDetailData() that consumes the Channel.
 *
 * If the data is not ready yet when the user clicks on the list, it is refetched
 * See refetchArtDetail.
 * activeDetailData is used to update the UI when the data is correctly updated.
* */
@HiltViewModel
class ArtViewModel @Inject constructor(
    private val artDataRepository: ArtDataRepository,
) : ViewModel() {
    // ------------------------------------------
    // user data variables
    // the list of packages installed on the device
    lateinit var listArt: Flow<PagingData<DataArtElement>>
    private val mapArtDetail: MutableMap<Int, DataArtDetail> = hashMapOf()

    // ------------------------------------------
    // UI state variables
    // The UI state for showing the first page with a spinner or not
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Empty)
    val uiState: StateFlow<UiState>
        get() = _uiState.asStateFlow()
    private val _activeRow: MutableStateFlow<Int> = MutableStateFlow(-1)
    val activeRow: StateFlow<Int>
        get() = _activeRow.asStateFlow()
    private val _activeDetailData: MutableStateFlow<DataArtDetail?> = MutableStateFlow(null)
    val activeDetailData: StateFlow<DataArtDetail?>
        get() = _activeDetailData.asStateFlow()
    // unbuffered channel, needed for concurrency data
    // there is no reason to limit the size of the channel, because we consume it on a limited number of tasks
    // limiting this size, could theoretically block the repository function that sends.
    private val artElementIndexesToProcess = Channel<Pair<Int, String>>(Channel.UNLIMITED)
    private val numberOfConcurrentDetailPrefetching = 10

    // ------------------------------------------
    // init variables
    private var isPagerinitialized = false

    // the view model survives configuration changes
    // But an activity stop will release the resources to free memory
    override fun onCleared() {
        super.onCleared()
        artElementIndexesToProcess.cancel()
        artDataRepository.onDestroy()
    }

    /*
      This startPagerAndDataFetching() function is not from the constructor, it is called manually the first time UiStateScreen is composed.
      And the initialized boolean prevents running more code in case of recomposition when the UI state changes.
      we need the activity instance to call this code.
      Note that even if we create the view model in the activity and call this startPagerAndDataFetching() function from the
      activity, recomposition or configuration change is possible and we need the initialized boolean anyways.
    * */
    fun startPagerAndDataFetching(unexpectedServerDataErrorString: String, owner: LifecycleOwner) {
        if (isPagerinitialized) {
            return
        }
        isPagerinitialized = true

        // one can add a RemoteMediator for caching
        // https://developer.android.com/topic/libraries/architecture/paging/v3-network-db
        listArt = Pager(PagingConfig(pageSize = AppConfig.pagingSize)) {
            ArtDataPagingSource(unexpectedServerDataErrorString, artDataRepository, artElementIndexesToProcess)
        }.flow
            .map { pagingData ->
                // This filtering is optional, it is just because the API send us duplicates.
                // We filter duplicate object numbers.
                // https://stackoverflow.com/questions/69284841/how-to-avoid-duplicate-items-in-pagingadapter
                // The museum API has a bug since duplicate pages can be returned
                // the 2 following calls give duplicates, and then the next pages are totally distinct
                // curl https://www.rijksmuseum.nl/api/en/collection?key=rIl6yb6x\&ps=3\&p=0
                // curl https://www.rijksmuseum.nl/api/en/collection?key=rIl6yb6x\&ps=3\&p=1
                val objectNumbersHashSet = hashSetOf<String>()
                val pagingDataWithoutDuplicates = pagingData.filter { elem ->
                    elem.objectNumber?.let { objectNumbersHashSet.add(elem.objectNumber) } ?: false
                }
                var itemIndex = -1
                pagingDataWithoutDuplicates.map { elem ->
                    itemIndex += 1
                    elem.objectNumber?.let {
                        // We send to the Channel the IDs that need to be prefetched for getting the detail data
                        artElementIndexesToProcess.send(Pair(itemIndex, elem.objectNumber))
                    }
                    elem
                }
            }
            .cachedIn(viewModelScope)

        // the repeatOnLifecycle is here to stop consuming resources when the app goes in background
        // it does not matter if we drop certain elements, because we refetch details if we click
        owner.lifecycleScope.launch(Dispatchers.IO) {
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                consumeChannelAndPrefetchInParallel(artElementIndexesToProcess)
                    // a cold flow is not executed without a collector
                    .collect()
            }
        }
    }

    private fun consumeChannelAndPrefetchInParallel(channel: Channel<Pair<Int, String>>): Flow<Unit> {
        val receiveFlow = channel.receiveAsFlow()
        return processInParallelToGetDetailData(receiveFlow)
    }

    // how to test: see the unit test in the test suite
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun processInParallelToGetDetailData(receiveFlow: Flow<Pair<Int, String>>): Flow<Unit> {
        return receiveFlow.flatMapMerge<Pair<Int, String>, Unit>(concurrency = numberOfConcurrentDetailPrefetching) { elementData ->
            flow {
                // how to test: comment this part so that there is no prefetching of the detail data
                // data is refetched when clicking on a row
                when (val resultDetail = artDataRepository.getArtObjectDetail(elementData.second)) {
                    is ResultOf.Success -> mapArtDetail.getOrPut(elementData.first) {
                        if (activeRow.value == elementData.first) {
                            setActiveDetailData(resultDetail.value)
                        }
                        resultDetail.value
                    }
                    else -> Unit
                }
                // for testing
                if (BuildConfig.DEBUG) {
                    emit(Unit)
                }
            }
        }
    }

    fun setActiveRow(owner: LifecycleOwner, rowId: Int) {
        owner.lifecycleScope.launch {
            _activeRow.emit(rowId)
        }
    }

    suspend fun setUiState(newUiState: UiState) {
        if (BuildConfig.DEBUG && newUiState is UiState.Error) {
            println(newUiState.error.message)
        }
        if (newUiState != _uiState) {
            _uiState.emit(newUiState)
        }
    }

    suspend fun setActiveDetailData(newActiveDetailData: DataArtDetail?) {
        if (newActiveDetailData != _activeDetailData.value) {
            _activeDetailData.emit(newActiveDetailData)
        }
    }

    fun getSavedArtDetail(rowId: Int): DataArtDetail? {
        return mapArtDetail[rowId]
    }

    suspend fun refetchArtDetail(rowId: Int, stateListArt: LazyPagingItems<DataArtElement>): DataArtDetail? {
        val itemData = stateListArt.itemSnapshotList[rowId]
        itemData?.objectNumber?.let { objectNumber ->
            when (val resultDetail = artDataRepository.getArtObjectDetail(objectNumber)) {
                is ResultOf.Success -> {
                    mapArtDetail.getOrPut(rowId) {
                        setActiveDetailData(resultDetail.value)
                        resultDetail.value
                    }
                    return resultDetail.value
                }
                else -> Unit
            }
        }
        return null
    }
}
