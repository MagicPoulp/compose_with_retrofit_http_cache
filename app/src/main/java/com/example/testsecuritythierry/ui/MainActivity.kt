package com.example.testsecuritythierry.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testsecuritythierry.ui.components.NavigationScreen
import com.example.testsecuritythierry.ui.components.ProgressIndicator
import com.example.testsecuritythierry.ui.components.UiStateScreen
import com.example.testsecuritythierry.ui.theme.TestSecurityThierryTheme
import com.example.testsecuritythierry.ui.view_models.MainActivityViewModel
import com.example.testsecuritythierry.ui.view_models.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainActivityViewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TestSecurityThierryTheme {
                // this extra UIState is because we want to prefetch the data before going to the
                // UiStateScreen
                val state by mainActivityViewModel.uiState.collectAsStateWithLifecycle()
                when (state) {
                    UiState.Filled -> UiStateScreen(activity = this)
                    else -> Row {
                        ProgressIndicator()
                    }
                }
            }
        }

        mainActivityViewModel.init(this)
        mainActivityViewModel.embedServer(this)
    }
}
