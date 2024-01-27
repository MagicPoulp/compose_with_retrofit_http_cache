package com.example.testcomposethierry.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import com.example.testcomposethierry.data.models.DataArtElement
import com.example.testcomposethierry.ui.components.uistate.ErrorScreen
import com.example.testcomposethierry.ui.components.uistate.ProgressIndicator
import com.example.testcomposethierry.ui.reusable_components.CenterAlignedText
import com.example.testcomposethierry.ui.view_models.DetailScreenViewModel
import com.example.testcomposethierry.ui.view_models.UiState

@Composable
fun DetailScreen(
    listArtPagingItems: LazyPagingItems<DataArtElement>,
    rowId: Int,
    detailScreenViewModel: DetailScreenViewModel,
) {
    val activeDetailData by detailScreenViewModel.activeDetailData.collectAsStateWithLifecycle()
    when (activeDetailData) {
        null -> ProgressIndicator()
        else -> activeDetailData?.plaqueDescription?.let { CenterAlignedText(it) }
            ?: ErrorScreen(UiState.Error(Throwable("Missing data")))
    }

    // This LaunchedEffect means that we want to do something immediately after composing the
    // DetailScreen
    LaunchedEffect(Unit) {
        // activeDetailData is used above to trigger the UI to show data
        // if there is no detail data from the mechanism with the Channel,
        // we fetch the detail data directly
        // Afterwards, we set activeDetailData
        val artDetail = detailScreenViewModel.getSavedArtDetail(rowId)
        artDetail?.let {
            detailScreenViewModel.setActiveDetailData(artDetail)
        } ?: run {
            // the Default dispatcher is used by default by LaunchedEffect
            // we do not need the IO dispatcher, because we need the result fast and the Default
            // is prioritized
            val newDetail = detailScreenViewModel.refetchArtDetail(rowId, listArtPagingItems)
            newDetail?.let { detail ->
                detailScreenViewModel.setActiveDetailData(detail)
            }
        }
    }
}
