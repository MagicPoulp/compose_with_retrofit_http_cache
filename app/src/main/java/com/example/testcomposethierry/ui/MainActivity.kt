package com.example.testComposethierry.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.testComposethierry.ui.components.UiStateScreen
import com.example.testComposethierry.ui.theme.TestComposeThierryTheme
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
