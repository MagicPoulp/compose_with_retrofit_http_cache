package com.example.testsecuritythierry.ui.components

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.paging.compose.LazyPagingItems
import com.example.testsecuritythierry.data.models.DataArtElement
import com.example.testsecuritythierry.ui.reusable_components.CenterAlignedText

@Composable
fun DetailScreen(
    stateListArt: LazyPagingItems<DataArtElement>,
    rowId: Int,
) {
    if (rowId > stateListArt.itemCount - 1 ) {
        CenterAlignedText("Error")
    }
    else
    {
        val itemData = stateListArt.itemSnapshotList[rowId]
        itemData?.detail?.plaqueDescription?.let { CenterAlignedText(it) }
    }
}
