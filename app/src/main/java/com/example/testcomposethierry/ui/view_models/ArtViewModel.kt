package com.example.testcomposethierry.ui.view_models


import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState {
    object Empty : UiState()
    object InProgress : UiState()
    class Error(val error: Throwable) : UiState()
    object Filled : UiState()
}

@HiltViewModel
class ArtViewModel @Inject constructor(
    private val artDataRepository: ArtDataRepository,
    private val persistentDataManager: PersistentDataManager,
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
    private var initialized = false

    @OptIn(ExperimentalCoroutinesApi::class)
    fun init(unexpectedServerDataErrorString: String, owner: LifecycleOwner) {
        if (initialized) {
            return
        }
        println("DB INIT")
        initialized = true

        // one can add a RemoteMediator for caching
        // https://developer.android.com/topic/libraries/architecture/paging/v3-network-db
        listArt = Pager(PagingConfig(pageSize = AppConfig.pagingSize)) {
            ArtDataPagingSource(unexpectedServerDataErrorString, artDataRepository, artElementIndexesToProcess)
        }.flow

        // the repeatOnLifecycle is here to stop consuming resources when the app goes in background
        // it does not matter if we drop certain elements, because we refetch details if we click
        owner.lifecycleScope.launch(Dispatchers.IO) {
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val receiveFlow = artElementIndexesToProcess.receiveAsFlow()
                processInParallelToGetDetailData(receiveFlow)
                    .collect()
            }
        }
    }

    // TOTEST: see the unit test in the test suite
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun processInParallelToGetDetailData(receiveFlow: Flow<Pair<Int, String>>): Flow<Unit> {
        return receiveFlow.flatMapMerge<Pair<Int, String>, Unit>(concurrency = numberOfConcurrentDetailPrefetching) { elementData ->
            flow {
                println("DB Start Parallel")
                // TOTEST: comment this so that there is no prefetching of the detail data
                // data is refetched when clicking on a row
                when (val resultDetail = artDataRepository.getArtObjectDetail(elementData.second)) {
                    is ResultOf.Success -> mapArtDetail.getOrPut(elementData.first) {
                        println("DB put from parallel prefetch " + elementData.first)
                        if (activeRow.value == elementData.first) {
                            println("°°°°°°°°°°>>>>>>>>>>>> HERE")
                            setActiveDetailData(resultDetail.value)
                        }
                        resultDetail.value
                    }
                    else -> Unit
                }
                println("DB TEST " + mapArtDetail[elementData.first])
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

    override fun onCleared() {
        super.onCleared()
        persistentDataManager.close()
        artElementIndexesToProcess.cancel()
    }

    fun getSavedArtDetail(rowId: Int): DataArtDetail? {
        println("DB get size " + mapArtDetail.size)
        println("DB get " + rowId + ", " + mapArtDetail[rowId])
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
