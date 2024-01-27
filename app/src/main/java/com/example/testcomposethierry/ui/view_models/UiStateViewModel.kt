package com.example.testcomposethierry.ui.view_models

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.example.testcomposethierry.BuildConfig
import com.example.testcomposethierry.data.config.AppConfig
import com.example.testcomposethierry.data.models.DataArtElement
import com.example.testcomposethierry.data.repositories.ArtDataPagingSource
import com.example.testcomposethierry.data.repositories.ArtDataRepository
import com.example.testcomposethierry.domain.network.RefetchArtDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

@HiltViewModel
class UiStateViewModel @Inject constructor(
    private val artDataRepository: ArtDataRepository,
) : ViewModel()
{
    // ------------------------------------------
    // UI state variables
    // The UI state for showing the first page with a spinner or not
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Empty)
    val uiState: StateFlow<UiState>
        get() = _uiState.asStateFlow()

    lateinit var listArt: Flow<PagingData<DataArtElement>>
    private var isPagerinitialized = false
    val artElementIndexesToProcess = Channel<Pair<Int, String>>(Channel.UNLIMITED)

    // ------------------------------------------

    suspend fun setUiState(newUiState: UiState) {
        if (BuildConfig.DEBUG && newUiState is UiState.Error) {
            println(newUiState.error.message)
        }
        if (newUiState != _uiState) {
            _uiState.emit(newUiState)
        }
    }

    /*
      This startPagerAndDataFetching() function is not from the constructor, it is called manually the first time UiStateScreen is composed.
      And the initialized boolean prevents running more code in case of recomposition when the UI state changes.
      we need the activity instance to call this code.
      Note that even if we create the view model in the activity and call this startPagerAndDataFetching() function from the
      activity, recomposition or configuration change is possible and we need the initialized boolean anyways.
    * */
    fun startPagerAndDataFetching(unexpectedServerDataErrorString: String) {
        if (isPagerinitialized) {
            return
        }
        isPagerinitialized = true

        // one can add a RemoteMediator for caching
        // https://developer.android.com/topic/libraries/architecture/paging/v3-network-db
        // or one can use the caching of HTTP requests themselves as we did in this project
        // (see PersistentDataManager)
        listArt = Pager(PagingConfig(pageSize = AppConfig.pagingSize)) {
            ArtDataPagingSource(unexpectedServerDataErrorString, artDataRepository)
        }
            .flow
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
                    elem.objectNumber?.let { objectNumbersHashSet.add(elem.objectNumber) }
                        ?: false
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
            // The cachedIn() operator makes the data stream shareable and caches the loaded data with the provided CoroutineScope. In any configuration change, it will provide the existing data instead of getting the data from scratch. It will also prevent memory leak.
            // https://medium.com/huawei-developers/what-is-paging3-mvvm-flow-databinding-hilt-d4fe6b1b11ec
            .cachedIn(viewModelScope)
    }

    // the view model survives configuration changes
    // But an activity stop will release the resources to free memory
    override fun onCleared() {
        super.onCleared()
        artElementIndexesToProcess.cancel()
        artDataRepository.onDestroy()
    }
}
