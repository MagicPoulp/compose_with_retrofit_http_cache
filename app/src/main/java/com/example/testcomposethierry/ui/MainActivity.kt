package com.example.testcomposethierry.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.testcomposethierry.ui.components.uistate.UiStateScreen
import com.example.testcomposethierry.ui.theme.TestComposeThierryTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mainActivityInternetErrorReporter: MainActivityInternetErrorReporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivityInternetErrorReporter.prepareInternetConnectivityErrorToaster(this)

        setContent {
            TestComposeThierryTheme {
                // we cannot use LocalContext.current
                UiStateScreen(activity = this)
            }
        }


    }
}
