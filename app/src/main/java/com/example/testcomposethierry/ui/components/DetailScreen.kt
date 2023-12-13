package com.example.testcomposethierry.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import com.example.testcomposethierry.data.models.DataArtElement
import com.example.testcomposethierry.ui.reusable_components.CenterAlignedText
import com.example.testcomposethierry.ui.view_models.ArtViewModel
import com.example.testcomposethierry.ui.view_models.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DetailScreen(
    stateListArt: LazyPagingItems<DataArtElement>,
    rowId: Int,
    artViewModel: ArtViewModel = hiltViewModel(),
) {
    val artDetail = artViewModel.getSavedArtDetail(rowId)
    val uiStateDetail by artViewModel.uiStateDetail.collectAsStateWithLifecycle()
    when (uiStateDetail) {
        is UiState.Filled ->  artDetail?.plaqueDescription?.let { CenterAlignedText(it) } ?: ProgressIndicator()
        else -> ProgressIndicator()
    }
    LaunchedEffect(Unit) {
        if (artDetail?.plaqueDescription == null) {
            withContext(Dispatchers.IO) {
                artViewModel.refetchArtDetail(rowId, stateListArt)
            }
        }
    }
}
