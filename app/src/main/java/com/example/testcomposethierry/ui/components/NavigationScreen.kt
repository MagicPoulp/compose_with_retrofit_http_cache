package com.example.testcomposethierry.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.LazyPagingItems
import com.example.testcomposethierry.data.models.DomainDataUsersListElement
import com.example.testcomposethierry.ui.setup.RoutingScreen
import com.example.testcomposethierry.ui.view_models.DetailScreenViewModel
import com.example.testcomposethierry.ui.view_models.ListScreenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NavigationScreen(
    activity: ComponentActivity,
    usersListPagingItems: LazyPagingItems<DomainDataUsersListElement>,
    listScreenViewModel: ListScreenViewModel = hiltViewModel(),
    detailScreenViewModel: DetailScreenViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    navController.addOnDestinationChangedListener { _, destination, _ ->
        CoroutineScope(Dispatchers.IO).launch {
            // needed to avoid seeing the old text in the Detail Screen
            // but we cannot set null when navigating to the Detail screen or it interfers with the data
            if (destination.route != RoutingScreen.MyDetailScreen.route) {
                detailScreenViewModel.setActiveDetailData(null)
            }
        }
    }
    val items = listOf(
        RoutingScreen.MyListScreen,
        // deactivated navigation to detail screen
        //RoutingScreen.MyDetailScreen,
    )
    val activeRow by listScreenViewModel.activeRow.collectAsStateWithLifecycle()
    Scaffold(
        backgroundColor = MaterialTheme.colors.secondary,
        bottomBar = {
            // deactivated bottom bar
//            BottomNavigation {
//                val navBackStackEntry by navController.currentBackStackEntryAsState()
//                val currentDestination = navBackStackEntry?.destination
//                items.forEach { screen ->
//                    BottomNavigationItem(
//                        icon = {
//                            Icon(
//                                imageVector = when(screen) {
//                                    RoutingScreen.MyListScreen -> Icons.Filled.Favorite
//                                    // deactivated navigation to detail screen
//                                    //RoutingScreen.MyDetailScreen -> Icons.Filled.AccountBox
//                                    else -> Icons.Filled.Favorite
//                                },
//                                contentDescription = null
//                            )
//                        },
//                        label = { Text(stringResource(screen.resourceId)) },
//                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
//                        // navigation from the bottom bar
//                        onClick = {
//                            // deactivated navigation to detail screen
////                            navController.navigate(screen.route) {
////                                // Pop up to the start destination of the graph to
////                                // avoid building up a large stack of destinations
////                                // on the back stack as users select items
////                                popUpTo(navController.graph.findStartDestination().id) {
////                                    saveState = true
////                                }
////
////                                // Avoid multiple copies of the same destination when
////                                // reselecting the same item
////                                launchSingleTop = true
////                            }
//                        }
//                    )
//                }
//            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = RoutingScreen.MyListScreen.route,
            androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(RoutingScreen.MyListScreen.route) {
                ListScreen(
                    activeRow = activeRow,
                    usersListPagingItems = usersListPagingItems,
                    navController = navController,
                )
            }

            // deactivated navigation to detail screen

//            // anomaly: as reported in this stack overflow, navigating, recomposes twice
//            // https://stackoverflow.com/questions/69190119/jetpack-compose-recompose-with-success-state-twice-when-exiting-current-composab?noredirect=1&lq=1
//            composable(RoutingScreen.MyDetailScreen.route) { backStackEntry ->
//                // this allows with the bottom back to go back and forth on previous active row
//                // and it starts at 0
//                val previousRow = if (activeRow != -1) activeRow else 0
//                val rowId = try {
//                    backStackEntry.arguments?.getString("rowId")?.toInt() ?: previousRow
//                } catch (_: Exception) {
//                    previousRow
//                }
//                DetailScreen(usersListPagingItems = usersListPagingItems, rowId = rowId, detailScreenViewModel = detailScreenViewModel)
//                // This LaunchedEffect is needed to mark in grey the active row in the main list
//                LaunchedEffect(Unit) {
//                    // we need a small delay so that we do not see the clicked row as active
//                    // with plain dark gray before having navigated to DetailScreen
//                    delay(300.milliseconds)
//                    if (rowId != activeRow) {
//                        listScreenViewModel.setActiveRow(owner = activity, rowId = rowId)
//                    }
//                }
//            }
        }
    }
}
