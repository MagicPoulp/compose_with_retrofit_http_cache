package com.example.testsecuritythierry.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.testsecuritythierry.R
import com.example.testsecuritythierry.ui.view_models.ArtViewModel
import com.example.testsecuritythierry.ui.view_models.UiState
import kotlinx.coroutines.flow.onCompletion

@Composable
fun UiStateScreen(
    newsViewModel: ArtViewModel = hiltViewModel(),
    activity: ComponentActivity,
) {
    newsViewModel.init(
        unexpectedServerDataErrorString = activity.resources.getString(R.string.unexpected_server_data)
    )
    Box(contentAlignment = Alignment.TopCenter,
        modifier = Modifier
        .fillMaxSize() ) {
        Row(modifier = Modifier
            .background(MaterialTheme.colors.primaryVariant)
            .fillMaxWidth()
            .fillMaxHeight()
        ) {
            val state by newsViewModel.uiState.collectAsStateWithLifecycle()
            val stateListArt = newsViewModel.listArt.collectAsLazyPagingItems()
            when (state) {
                UiState.Filled -> NavigationScreen(activity = activity, stateListArt = stateListArt)
                else -> Row {
                    ProgressIndicator()
                }
            }

            // LazyPagingItems cannot be collected in the ViewModel, but it can be in a LaunchedEffect
            // https://developer.android.com/jetpack/compose/side-effects#snapshotFlow
            LaunchedEffect(Unit) {
                snapshotFlow { stateListArt.itemSnapshotList.count() }
                    .collect {
                        val newState = when (it) {
                            0 -> UiState.Empty
                            else -> UiState.Filled
                        }
                        newsViewModel.setUiState(newState)
                    }
            }
        }
    }
}

@Composable
fun ProgressIndicator() {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.secondary)
    ) {
        CircularProgressIndicator()
    }
}