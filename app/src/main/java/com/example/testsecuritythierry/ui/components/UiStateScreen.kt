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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.testsecuritythierry.R
import com.example.testsecuritythierry.ui.view_models.ArtViewModel
import com.example.testsecuritythierry.ui.view_models.UiState
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

@Composable
fun UiStateScreen(
    artViewModel: ArtViewModel = hiltViewModel(),
    activity: ComponentActivity,
) {
    val unexpectedServerDataErrorString = activity.resources.getString(R.string.unexpected_server_data)
    artViewModel.init(
        unexpectedServerDataErrorString = unexpectedServerDataErrorString
    )
    Box(contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .background(MaterialTheme.colors.secondary)
            .fillMaxSize() ) {
        Row(modifier = Modifier
            .background(MaterialTheme.colors.primaryVariant)
            .fillMaxWidth()
            .fillMaxHeight()
        ) {
            val state by artViewModel.uiState.collectAsStateWithLifecycle()
            val stateListArt = artViewModel.listArt.collectAsLazyPagingItems()
            when (state) {
                // put all on ErrorScreen() to debug the error screen
                is UiState.Empty -> ProgressIndicator()
                UiState.Filled -> NavigationScreen(activity = activity, stateListArt = stateListArt)
                is UiState.Error -> ErrorScreen(state)
                else -> Row {
                    ProgressIndicator()
                }
            }

            LaunchedEffect(stateListArt.loadState) {
                stateListArt.apply {
                    if (loadState.refresh is LoadState.Error) {
                        artViewModel.setUiState(UiState.Error((loadState.refresh as LoadState.Error).error))
                        return@apply
                    }
                    if (loadState.append is LoadState.Error) {
                        artViewModel.setUiState(UiState.Error((loadState.append as LoadState.Error).error))
                        return@apply
                    }
                }
            }

            // LazyPagingItems cannot be collected in the ViewModel, but it can be in a LaunchedEffect
            // https://developer.android.com/jetpack/compose/side-effects#snapshotFlow
            LaunchedEffect(Unit) {
                snapshotFlow { stateListArt.itemSnapshotList.count() }
                    .collect { listSize ->
                        val loadState = stateListArt.loadState
                        if (loadState.refresh is LoadState.Error || loadState.append is LoadState.Error) {
                            return@collect
                        }
                        val newState = when (listSize) {
                            0 -> UiState.Empty
                            else -> UiState.Filled
                        }
                        artViewModel.setUiState(newState)
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