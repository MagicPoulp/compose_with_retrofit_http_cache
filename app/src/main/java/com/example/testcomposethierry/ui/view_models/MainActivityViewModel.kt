package com.example.testcomposethierry.ui.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.compose.LazyPagingItems
import com.example.testcomposethierry.data.http.NetworkConnectionManager
import com.example.testcomposethierry.data.models.DataUsersListElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val networkConnectionManager: NetworkConnectionManager
) : ViewModel() {

    // stateIn adds a small delay to skip screen rotations
    // https://medium.com/androiddevelopers/migrating-from-livedata-to-kotlins-flow-379292f419fb
    val isConnectedCheckLoop: StateFlow<Int> = networkConnectionManager.isConnectedCheckLoopFlow
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(2000),
            initialValue = 1
        )
}
