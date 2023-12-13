package com.example.testcomposethierry.ui.view_models


import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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
    val mapArtDetail: MutableMap<Int, DataArtDetail> = hashMapOf()

    // ------------------------------------------
    // UI state variables
    // The UI state for showing the first page with a spinner or not
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Empty)
    val uiState: StateFlow<UiState>
        get() = _uiState.asStateFlow()
    private val _activeRow: MutableStateFlow<Int> = MutableStateFlow(-1)
    val activeRow: StateFlow<Int>
        get() = _activeRow.asStateFlow()
    // unbuffered channel, needed for concurrency data
    // there is no reason to limit the size of the channel, because we consume it on a limited number of tasks
    // limiting this size, could theoretically block the repository function that sends.
    private val artElementIndexesToProcess = Channel<Pair<Int, String>>(Channel.UNLIMITED)

    // ------------------------------------------
    // init variables
    private var initialized = false

    @OptIn(ExperimentalCoroutinesApi::class)
    fun init(unexpectedServerDataErrorString: String, owner: LifecycleOwner) {
        if (initialized) {
            return
        }
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
                receiveFlow.flatMapMerge<Pair<Int, String>, Unit>(concurrency = 10) { elementData ->
                    flow {
                        when (val resultDetail = artDataRepository.getArtObjectDetail(elementData.second)) {
                            //is ResultOf.Success -> mapArtDetail.getOrPut(elementData.first) { resultDetail.value }
                            else -> Unit
                        }
                    }
                }
                // we do not emit and we do not collect the flow above
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

    override fun onCleared() {
        super.onCleared()
        persistentDataManager.close()
        artElementIndexesToProcess.cancel()
    }

    fun closeChannel() {
        artElementIndexesToProcess.cancel()
    }

    fun getSavedArtDetail(rowId: Int): DataArtDetail? {
        return mapArtDetail[rowId]
    }
}
