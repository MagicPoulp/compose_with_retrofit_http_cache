package com.example.testcomposethierry.ui.components.uistate

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
import com.example.testcomposethierry.R
import com.example.testcomposethierry.ui.components.NavigationScreen
import com.example.testcomposethierry.ui.view_models.UiState
import com.example.testcomposethierry.ui.view_models.UiStateScreenViewModel

@Composable
fun UiStateScreen(
    /*
    From the documentation in the source code of hiltViewModel(), it is clear that the result
    of hiltViewModel() is unique and survives configuration changes
    Here is are not in a navigation graph, hence the view model is scoped the the activity

    The doc says: "Returns an existing HiltViewModel  -annotated ViewModel or creates a new one scoped to the
    current navigation graph present on the {@link NavController} back stack.
    If no navigation graph is currently present then the current scope will be used, usually, a fragment or an activity."

    --> Experience to test that it is true:

    Add a breakpoint in an init in the view Model
    init {
        println("INIT VIEW MODEL")
    }

    restart the activity in the onCreate of the activity
            Handler().postDelayed({
            onPause()
            onStop()
            onRestart()

        }, 10000)

    And observe that the hilt view model survives the configuration change.
    Because the init from the constructor call is not called more than once.
    */
    uiStateScreenViewModel: UiStateScreenViewModel = hiltViewModel(),
    activity: ComponentActivity,
) {
    val unexpectedServerDataErrorString = activity.resources.getString(R.string.unexpected_server_data)
    uiStateScreenViewModel.startPagerAndDataFetching(
        unexpectedServerDataErrorString = unexpectedServerDataErrorString,
    )
    val state by uiStateScreenViewModel.uiState.collectAsStateWithLifecycle()
    val usersListPagingItems = uiStateScreenViewModel.usersList.collectAsLazyPagingItems()
    Box(contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .background(MaterialTheme.colors.secondary)
            .fillMaxSize() ) {
        Row(modifier = Modifier
            .background(MaterialTheme.colors.primaryVariant)
            .fillMaxWidth()
            .fillMaxHeight()
        ) {
            when (state) {
                // put each case here on ErrorScreen() to debug the error screen
                is UiState.Empty -> ProgressIndicator()
                UiState.Filled -> NavigationScreen(
                    activity = activity,
                    usersListPagingItems = usersListPagingItems,
                )
                is UiState.Error -> ErrorScreen(state)
                else -> Row {
                    ProgressIndicator()
                }
            }
            /*
            LoadResult.Error will result in a changed stateusersList.loadState
            If the lazy loading has an error state, we update the UI State to an error
            Excerpt from androidx.paging / PagingSource.kt
         public sealed class LoadResult<Key : Any, Value : Any> {
         * Error result object for [PagingSource.load].
         *
         * This return type indicates an expected, recoverable error (such as a network load
         * failure). This failure will be forwarded to the UI as a [LoadState.Error], and may be
         * retried.
         *
         * @sample androidx.paging.samples.pageKeyedPagingSourceSample
             public data class Error<Key : Any, Value : Any>(
                 val throwable: Throwable
             ) : PagingSource.LoadResult<Key, Value>()
            */
            LaunchedEffect(usersListPagingItems.loadState) {
                usersListPagingItems.apply {
                    if (loadState.refresh is LoadState.Error) {
                        uiStateScreenViewModel.setUiState(UiState.Error((loadState.refresh as LoadState.Error).error))
                        return@apply
                    }
                    if (loadState.append is LoadState.Error) {
                        //uiStateScreenViewModel.setUiState(UiState.Error((loadState.append as LoadState.Error).error))
                        return@apply
                    }
                }
            }

            // collectAsLazyPagingItems() cannot work in the viewModel, it must lie in a Composable function
            // LazyPagingItems cannot be collected in the ViewModel, but it can be in a LaunchedEffect
            // https://developer.android.com/jetpack/compose/side-effects#snapshotFlow
            LaunchedEffect(Unit) {
                snapshotFlow { usersListPagingItems.itemSnapshotList.count() }
                    .collect { listSize ->
                        val loadState = usersListPagingItems.loadState
                        if (loadState.refresh is LoadState.Error || loadState.append is LoadState.Error) {
                            return@collect
                        }
                        val newState = when (listSize) {
                            0 -> UiState.Empty
                            else -> UiState.Filled
                        }
                        uiStateScreenViewModel.setUiState(newState)
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
