package com.example.testcomposethierry.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.testcomposethierry.R
import com.example.testcomposethierry.data.http.NetworkConnectionManager
import com.example.testcomposethierry.ui.components.uistate.UiStateScreen
import com.example.testcomposethierry.ui.theme.TestComposeThierryTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkConnectionManager: NetworkConnectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val self = this
        lifecycleScope.launch(Dispatchers.IO) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkConnectionManager.isConnected
                    // a cold flow is not executed without a collector
                    .collect { isConnected ->
                        if (!isConnected) {
                            launch(Dispatchers.Main) {
                                Toast.makeText(self, resources.getString(R.string.internet_connectivity_error), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }

        setContent {
            TestComposeThierryTheme {
                UiStateScreen(activity = this)
            }
        }
    }
}
