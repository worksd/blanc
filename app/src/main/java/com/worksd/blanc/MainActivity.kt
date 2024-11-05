package com.worksd.blanc

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.gson.Gson
import com.worksd.blanc.data.BootInfoResponse
import com.worksd.blanc.ui.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val webViewInterface = WebAppInterface(this)
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val bootInfo: BootInfoResponse =
                gson.fromJson(intent.getStringExtra(EXTRAS_BOOT_INFO), BootInfoResponse::class.java)

            val bottomMenuList = bootInfo.bottomMenuList

            setContent {
                MainScreen(
                    activity = this,
                    bottomMenuList = bottomMenuList,
                    webViewInterface = webViewInterface,
                )
            }
        } catch (e: Exception) {
            Log.d("MainActivity", "onCreate: $e")
        }
    }
}
