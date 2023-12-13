package com.example.testcomposethierry.ui.setup

import androidx.annotation.StringRes
import com.example.testcomposethierry.R

// https://developer.android.com/jetpack/compose/navigation#create-navhost
sealed class RoutingScreen(val route: String, @StringRes val resourceId: Int) {
    object MyListScreen : RoutingScreen("my_list_screen", R.string.my_list_screen)
    object MyDetailScreen : RoutingScreen("my_detail_screen/{rowId}", R.string.my_detail_screen)
}
