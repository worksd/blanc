package com.worksd.blanc.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.webkit.WebResourceError
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.worksd.blanc.EventReceiver
import com.worksd.blanc.WebViewListener
import com.worksd.blanc.client.CustomWebViewClient
import com.worksd.blanc.data.BootInfoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val INITIAL_SCREEN_ROUTE = "splash"
const val MAIN_SCREEN_ROUTE = "main"
const val DEFAULT_SCREEN_ROUTE = "default"

@SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
@Composable
fun BlancScreen(
    activity: Activity,
    viewModel: MainViewModel,
) {
    val coroutineScope = rememberCoroutineScope()
    val navController: NavHostController = rememberNavController()
    val errorDialogVisible = remember { mutableStateOf(false) }

    val webinterface = remember {
        object : EventReceiver {
            override fun navigate(route: String) {
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                }
            }

            override fun pushAndAllClear(route: String) {
                coroutineScope.launch(Dispatchers.Main) {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }

            override fun fetchBootInfo(bootInfo: String) {
                try {
                    val res: BootInfoResponse =
                        Gson().fromJson(bootInfo, BootInfoResponse::class.java)
                    viewModel.setBottomMenuList(res.bottomMenuList)
                    pushAndAllClear(res.route)
                } catch (e: Exception) {
                    Log.d("WebAppInterface", "fetchBootInfo: $e")
                }
            }
        }
    }

    val client = remember {
        CustomWebViewClient(
            context = activity,
            listener = object: WebViewListener {
                override fun onSplashPageStarted() {

                }

                override fun onConnectFail() {
                    errorDialogVisible.value = true
                }

                override fun onConnectSuccess() {

                }
            },
        )
    }


    NavHost(
        navController = navController,
        startDestination = INITIAL_SCREEN_ROUTE,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {

        composable(INITIAL_SCREEN_ROUTE) {
            SplashScreen(
                listener = webinterface,
                client = client,
                onLoadingStart = {
                    viewModel.startConnectTimer()
                }
            )
        }

        composable(MAIN_SCREEN_ROUTE) {
            MainScreen(
                bottomMenuList = viewModel.bottomMenuList.collectAsState().value,
                listener = webinterface,
                client = client,
            )
        }

        composable(DEFAULT_SCREEN_ROUTE) {
            WebScreen(
                route = navController.currentDestination?.route.orEmpty(),
                webInterface = webinterface,
                client = client,
            )
        }
    }

    if (errorDialogVisible.value) {
        Dialog(onDismissRequest = {
            errorDialogVisible.value = false
        }) {
            Box(
                modifier = Modifier.width(300.dp).height(200.dp).background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Connection Error",
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                )
            }
        }
    }
}