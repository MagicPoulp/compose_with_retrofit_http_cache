package com.example.testcomposethierry.ui.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testcomposethierry.data.data_sources.RealmDatabaseDataSource
import com.example.testcomposethierry.data.http.NetworkConnectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val networkConnectionManager: NetworkConnectionManager,
    private val realmDatabaseDataSource: RealmDatabaseDataSource,
) : ViewModel() {

    // stateIn adds a small delay to skip screen rotations
    // https://medium.com/androiddevelopers/migrating-from-livedata-to-kotlins-flow-379292f419fb
    val isConnectedCheckLoop: StateFlow<Int> = networkConnectionManager.isConnectedCheckLoopFlow
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(2000),
            initialValue = 1
        )

    override fun onCleared() {
        super.onCleared()
        realmDatabaseDataSource.close()
    }
}
