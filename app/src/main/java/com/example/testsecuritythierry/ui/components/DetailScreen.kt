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
        /*
        val html = itemData?.html
        html?.let {
            AndroidView(
                factory = {
                    WebView(it).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        webViewClient = WebViewClient()
                        loadData(html, "text/html", "UTF-8")
                    }
                }
            )
        } ?: run {
            CenterAlignedText("Missing HTML, please try another row.")
        }
    */
    }
}
