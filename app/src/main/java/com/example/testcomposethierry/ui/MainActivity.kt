package com.example.testcomposethierry.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.testcomposethierry.ui.components.uistate.UiStateScreen
import com.example.testcomposethierry.ui.theme.TestComposeThierryTheme
import com.example.testcomposethierry.ui.view_models.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mainActivityInternetMonitorer: MainActivityInternetMonitorer

    // https://developer.android.com/training/dependency-injection/hilt-jetpack#viewmodels
    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivityInternetMonitorer.prepareInternetConnectivityCheckLoop(this, mainActivityViewModel)
        mainActivityInternetMonitorer.prepareInternetConnectivityErrorToaster(this)

        setContent {
            TestComposeThierryTheme {
                // we cannot use LocalContext.current
                UiStateScreen(activity = this)
            }
        }


    }
}
