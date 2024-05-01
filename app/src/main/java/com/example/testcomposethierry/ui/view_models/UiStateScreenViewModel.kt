package com.example.testcomposethierry.ui.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.testcomposethierry.BuildConfig
import com.example.testcomposethierry.data.config.AppConfig
import com.example.testcomposethierry.data.UsersDataPagingSource
import com.example.testcomposethierry.data.models.DataUsersListElement
import com.example.testcomposethierry.data.repositories.UsersListDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UiStateScreenViewModel @Inject constructor(
    private val usersListDataRepository: UsersListDataRepository,
) : ViewModel()
{
    // UI state variables
    // The UI state for showing the first page with a spinner or not
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Empty)
    val uiState: StateFlow<UiState>
        get() = _uiState.asStateFlow()

    lateinit var usersList: Flow<PagingData<DataUsersListElement>>
    private var isPagerinitialized = false

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
        usersList = Pager(PagingConfig(pageSize = AppConfig.pagingSize)) {
            UsersDataPagingSource(unexpectedServerDataErrorString, usersListDataRepository)
        }
            .flow
            // The cachedIn() operator makes the data stream shareable and caches the loaded data with the provided CoroutineScope. In any configuration change, it will provide the existing data instead of getting the data from scratch. It will also prevent memory leak.
            // https://medium.com/huawei-developers/what-is-paging3-mvvm-flow-databinding-hilt-d4fe6b1b11ec
            .cachedIn(viewModelScope)
    }
}
