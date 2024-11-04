package com.worksd.blanc

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun BlancWebView(
    activity: Activity,
    route: String,
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            // WebView 설정 초기화
            settings.javaScriptEnabled = true
            this.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(resources.getColor(android.R.color.black, null))
            this.webViewClient = CustomWebViewClient(context)
            settings.javaScriptEnabled = true
            settings.allowContentAccess = true
            settings.domStorageEnabled = true

            addJavascriptInterface(WebAppInterface(activity), "KloudEvent")
        }
    }
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), factory = {
            webView
        }, update = {
            val url = "http://192.168.0.37:3000/$route"
            it.loadUrl(url)
        })

}
