package com.worksd.blanc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = intent?.data?.getQueryParameter("name") ?: "undefined"
        setContent {
            MainScreen(
                activity = this
            )
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        Log.d("DORODORO", " asdasfs")
    }
}

@Composable
private fun MainScreen(
    activity: Activity,
) {

    val navController = rememberNavController()
    var navigationSelectedItem by remember {
        mutableStateOf(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        bottomBar = {
        NavigationBar {
            BottomNavigationItem().bottomNavigationItems().forEachIndexed { index, navigationItem ->
                NavigationBarItem(
                    selected = index == navigationSelectedItem,
                    label = {
                        Text(navigationItem.label)
                    },
                    icon = {
                        Image(
                            navigationItem.icon,
                            contentDescription = navigationItem.label
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
        Log.d("DORODORO", "out recomposition")
        NavHost(
            navController = navController,
            startDestination = "/home",
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            modifier = Modifier.padding(paddingValues = paddingValues)
        ) {
            Log.d("DORODORO", "in recomposition")
            BottomNavigationItem().bottomNavigationItems().forEach { item ->
                composable(item.url) {
                    BlancWebView(
                        activity = activity,
                        route = item.url
                    )
                }
            }
        }
    }
}

data class BottomNavigationItem(
    val label: String = "",
    val icon: ImageVector = Icons.Filled.Home,
    val url: String = ""
) {
    fun bottomNavigationItems(): List<BottomNavigationItem> {
        return listOf(
            BottomNavigationItem(
                label = "Home",
                icon = Icons.Filled.Home,
                url = "/home"
            ),
            BottomNavigationItem(
                label = "Notification",
                icon = Icons.Filled.Notifications,
                url = "/notifications"
            ),
            BottomNavigationItem(
                label = "Setting",
                icon = Icons.Filled.Settings,
                url = "/setting"
            ),
        )
    }
}