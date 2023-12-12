package com.example.testsecuritythierry.ui.view_models


import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.testsecuritythierry.data.config.AppConfig
import com.example.testsecuritythierry.data.models.DataNewsElement
import com.example.testsecuritythierry.data.repositories.LocalNewsDataRepository
import com.example.testsecuritythierry.data.repositories.NewsDataPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState {
    object Empty : UiState()
    object InProgress : UiState()
    object Error : UiState()
    object Filled : UiState()
}

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val localNewsDataRepository: LocalNewsDataRepository,
) : ViewModel() {
    // the list of packages installed on the device
    lateinit var listNews: Flow<PagingData<DataNewsElement>>
    // The UI state for showing the first page with a spinner or not
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Empty)
    val uiState: StateFlow<UiState>
        get() = _uiState.asStateFlow()
    private val _activeRow: MutableStateFlow<Int> = MutableStateFlow(-1)
    val activeRow: StateFlow<Int>
        get() = _activeRow.asStateFlow()

    // ------------------------------------------
    // non flow variables
    private var initialized = false

    fun init(unexpectedServerDataErrorString: String) {
        if (initialized) {
            return
        }
        initialized = true

        // one can add a RemoteMediator for caching
        // https://developer.android.com/topic/libraries/architecture/paging/v3-network-db
        listNews = Pager(PagingConfig(pageSize = AppConfig.pagingSize)) {
            NewsDataPagingSource(unexpectedServerDataErrorString, localNewsDataRepository)
        }.flow
    }

    fun setActiveRow(owner: LifecycleOwner, rowId: Int) {
        owner.lifecycleScope.launch {
            _activeRow.emit(rowId)
        }
    }

    suspend fun setUiState(newUiState: UiState) {
        _uiState.emit(newUiState)
    }
}
