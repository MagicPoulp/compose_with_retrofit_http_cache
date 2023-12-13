package com.example.testcomposethierry.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
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
    artViewModel: ArtViewModel,
) {
    println("DB RENDER °°°")
    val activeDetailData by artViewModel.activeDetailData.collectAsStateWithLifecycle()
    val artDetail = artViewModel.getSavedArtDetail(rowId)
    if (artDetail == null && activeDetailData == null) {
        CenterAlignedText("NULL")
    }
    else {
        activeDetailData?.plaqueDescription?.let { CenterAlignedText(it) } ?: ProgressIndicator()
    }

    LaunchedEffect(Unit) {
        println("DB LAUNCH °°°")
        artDetail?.plaqueDescription?.let {
            artViewModel.setActiveDetailData(artDetail)
        } ?: run {
            withContext(Dispatchers.IO) {
                //val newDetail = artViewModel.refetchArtDetail(rowId, stateListArt)
                //newDetail?.let { detail ->
                //    artViewModel.setActiveDetailData(detail)
                //}
            }
        }
    }
}
