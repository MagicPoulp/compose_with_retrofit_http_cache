package com.example.testsecuritythierry.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.testsecuritythierry.ui.components.UiStateScreen
import com.example.testsecuritythierry.ui.theme.TestSecurityThierryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TestSecurityThierryTheme {
                UiStateScreen(activity = this)
            }
        }
    }
}
