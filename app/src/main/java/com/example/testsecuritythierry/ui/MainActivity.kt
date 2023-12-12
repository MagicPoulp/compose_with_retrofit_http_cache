package com.example.testsecuritythierry.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.testsecuritythierry.ui.components.UiStateScreen
import com.example.testsecuritythierry.ui.theme.TestSecurityThierryTheme
import com.example.testsecuritythierry.ui.view_models.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainActivityViewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TestSecurityThierryTheme {
                UiStateScreen(activity = this)
            }
        }

        mainActivityViewModel.init(this)
        mainActivityViewModel.embedServer(this)
    }
}
