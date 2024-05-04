package com.example.testcomposethierry.ui

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.testcomposethierry.R
import com.example.testcomposethierry.data.http.NetworkConnectionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

// this must not be a view model since a viewModel must not store an activity's reference
class MainActivityInternetErrorReporter @Inject constructor(
    private val networkConnectionManager: NetworkConnectionManager,
) : ViewModel() {
    fun prepareInternetConnectivityErrorToaster(activity: Activity) {
        val internetConnectivityErrorString = activity.resources.getString(R.string.error_internet_connectivity_error)
        val owner = activity as LifecycleOwner
        // this allows to pause during configuration changes, and when the app goes in the background
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

    private fun showInternetConnectivityErrorToast(activity: Activity, internetConnectivityErrorString: String) {
        runBlocking {
            launch(Dispatchers.Main) {
                Toast.makeText(activity, internetConnectivityErrorString, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        networkConnectionManager.unregister()
    }
}