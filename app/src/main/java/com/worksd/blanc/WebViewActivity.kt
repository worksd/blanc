package com.worksd.blanc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.worksd.blanc.ui.BlancWebView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebViewActivity : ComponentActivity() {
    private val webViewInterface = WebAppInterface(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BlancWebView(
                activity = this,
                webViewInterface = webViewInterface,
                route = intent?.getStringExtra(EXTRAS_ROUTE).orEmpty(),
            )
        }
    }
}