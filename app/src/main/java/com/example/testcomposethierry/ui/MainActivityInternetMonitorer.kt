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
import com.example.testcomposethierry.ui.view_models.MainActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

// this must not be a view model since a viewModel must not store an activity's reference
class MainActivityInternetMonitorer @Inject constructor(
    private val networkConnectionManager: NetworkConnectionManager,
) : ViewModel() {

    fun prepareInternetConnectivityCheckLoop(activity: Activity, mainActivityViewModel: MainActivityViewModel) {
        val owner = activity as LifecycleOwner
        // this allows to pause during configuration changes, and when the app goes in the background
        owner.lifecycleScope.launch(Dispatchers.IO) {
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                //assert(Looper.myLooper() != Looper.getMainLooper())
                mainActivityViewModel.isConnectedCheckLoop
                    // a hot flow must be collected to be active
                    .collect {
                        // System.err.println(it)
                    }
            }
        }
    }

    // we could combine the 2 functions into one repeatOnLifecycle
    // however, it is preferable to separate the 2 collects because the 2 events are independent
    // the one above loops to check internet
    // the one below happens when internet connectivity changes
    // this is similare to the interface segregation principle, separate things should be separate
    // combining the 2 only micro-optimizes
    //
    // Moreover, we have a unit test, and separating things makes unit tests simpler
    fun prepareInternetConnectivityErrorToaster(activity: Activity) {
        val internetConnectivityErrorString = activity.resources.getString(R.string.error_internet_connectivity_error)
        val owner = activity as LifecycleOwner
        // this allows to pause during configuration changes, and when the app goes in the background
        owner.lifecycleScope.launch(Dispatchers.IO) {
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkConnectionManager.isConnected
                    .collect { isConnected ->
                        // this assert fails if we change the above dispatcher to Dispatchers.Main
                        //assert(Looper.myLooper() != Looper.getMainLooper())
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