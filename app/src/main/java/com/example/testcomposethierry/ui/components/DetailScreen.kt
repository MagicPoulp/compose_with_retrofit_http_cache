package com.example.testcomposethierry.ui.components

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testcomposethierry.ui.reusable_components.CenterAlignedText
import com.example.testcomposethierry.ui.view_models.ArtViewModel

@Composable
fun DetailScreen(
    rowId: Int,
    artViewModel: ArtViewModel = hiltViewModel(),
) {
    val artDetail = artViewModel.getSavedArtDetail(rowId)
    artDetail?.plaqueDescription?.let { CenterAlignedText(it) } ?: ProgressIndicator()
}
