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
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TestComposeThierryTheme {
                UiStateScreen(activity = this)
            }
        }
    }
}
