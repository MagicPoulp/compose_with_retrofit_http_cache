package com.example.testcomposethierry.ui.view_models


import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.testcomposethierry.R
import com.example.testcomposethierry.data.http.NetworkConnectionManager
import com.example.testcomposethierry.data.repositories.UsersListDataRepository
import com.example.testcomposethierry.ui.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

sealed class UiState {
    object Empty : UiState()
    object InProgress : UiState()
    class Error(val error: Throwable) : UiState()
    object Filled : UiState()
}

@HiltViewModel
class ListScreenViewModel @Inject constructor(
    private val usersListDataRepository: UsersListDataRepository,
    private val networkConnectionManager: NetworkConnectionManager,
) : ViewModel() {
    private val _activeRow: MutableStateFlow<Int> = MutableStateFlow(-1)
    val activeRow: StateFlow<Int>
        get() = _activeRow.asStateFlow()

    // ------------------------------------------

    fun prepareInternetConnectivityErrorToaster(activity: Activity, internetConnectivityErrorString: String) {
        val owner = activity as LifecycleOwner
        owner.lifecycleScope.launch(Dispatchers.IO) {
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkConnectionManager.isConnected
                    .collect { isConnected ->
                        if (!isConnected) {
                            showInternetConnectivityErrorToast(activity, internetConnectivityErrorString)
                        }
                    }
            }
        }
    }

    fun showInternetConnectivityErrorToast(activity: Activity, internetConnectivityErrorString: String) {
        runBlocking {
            launch(Dispatchers.Main) {
                Toast.makeText(activity, internetConnectivityErrorString, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setActiveRow(owner: LifecycleOwner, rowId: Int) {
        owner.lifecycleScope.launch {
            _activeRow.emit(rowId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        networkConnectionManager.unregister()
    }
}
