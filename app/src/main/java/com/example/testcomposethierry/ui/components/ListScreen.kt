package com.example.testcomposethierry.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import com.example.testcomposethierry.data.models.DataUsersListElement
import com.example.testcomposethierry.ui.reusable_components.LeftAlignedText
import com.example.testcomposethierry.ui.setup.RoutingScreen

val horizontalMargin = 20.dp
val rowHeight = 60.dp
//const val goldenNumber = 1.618

@Composable
fun ListScreen(
    activeRow: Int,
    usersListPagingItems: LazyPagingItems<DataUsersListElement>,
    navController: NavController,
) {
    // keep the scrolling state upon screen rotation
    val listState = rememberLazyListState()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.secondary
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.secondary),
            contentPadding = PaddingValues(horizontal = horizontalMargin),
        ) {
            // as stated at the link below, items(stateusersList.itemSnapshotList) does not work with AppConfig.pagingSize = 10, and total items = 48
            // but it works using the itemsIndex alternative
            // https://stackoverflow.com/questions/75960184/why-jetpack-compose-room-offline-pagination-not-loading-next-page
            //items(stateusersList.itemSnapshotList) {item ->
            //item?.let {
            //    TableItemRow(item)
            //}
            //}
            itemsIndexed(usersListPagingItems.itemSnapshotList) { index, _ ->
                usersListPagingItems[index]?.let { TableItemRow(activeRow = activeRow, item = it, index = index, navController = navController) }
            }
        }
    }
}

@Composable
fun TableItemRow(
    activeRow: Int,
    item: DataUsersListElement,
    index: Int,
    navController: NavController,
) {
    val isPreviousActiveRow = index == activeRow
    Box(
        modifier = Modifier
            // the last clicked row is in dark gray
            .background(color = if (isPreviousActiveRow) MaterialTheme.colors.primary else MaterialTheme.colors.secondary)
            .height(rowHeight),
        contentAlignment = Alignment.Center) {
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.clickable {
                navController.navigate(RoutingScreen.MyDetailScreen.route.replace("{rowId}", "$index"))
            }
        ) {
            item.email.isNotBlank().let { LeftAlignedText(text = item.email) }
        }
        Divider(
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.dp)
        )
    }
}
