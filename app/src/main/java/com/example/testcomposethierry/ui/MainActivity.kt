package com.example.testcomposethierry.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.testcomposethierry.ui.components.uistate.UiStateScreen
import com.example.testcomposethierry.ui.theme.TestComposeThierryTheme
import dagger.hilt.android.AndroidEntryPoint

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
