package com.worksd.blanc.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.worksd.blanc.client.CustomWebViewClient
import com.worksd.blanc.EventReceiver
import com.worksd.blanc.data.BottomMenuResponse


@Composable
fun MainScreen(
    client: CustomWebViewClient,
    listener: EventReceiver,
    bottomMenuList: List<BottomMenuResponse>,
) {

    val navController = rememberNavController()
    var navigationSelectedItem by remember {
        mutableStateOf(0)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        bottomBar = {
            NavigationBar {
                bottomMenuList.forEachIndexed { index, navigationItem ->
                    NavigationBarItem(
                        selected = index == navigationSelectedItem,
                        label = {
                            Text(
                                text = navigationItem.label,
                                color = Color.Black,
                                fontSize = navigationItem.labelSize.sp,
                            )
                        },
                        icon = {
                            AsyncImage(
                                modifier = Modifier.size(navigationItem.iconSize.dp),
                                model = navigationItem.iconUrl,
                                contentDescription = "Bottom Navigation Icon",
                            )
                        },
                        onClick = {
                            navigationSelectedItem = index
                            navController.navigate(navigationItem.url) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = bottomMenuList[0].url,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            modifier = Modifier.padding(paddingValues = paddingValues)
        ) {
            bottomMenuList.forEach { item ->
                composable(item.url) {
                    WebScreen(
                        webInterface = listener,
                        route = item.url,
                        client = client,
                    )
                }
            }
        }
    }
}