package com.worksd.blanc.ui

import android.app.Activity
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.worksd.blanc.CustomWebViewClient
import com.worksd.blanc.WebAppInterface

@Composable
fun BlancWebView(
    activity: Activity,
    route: String,
    webViewInterface: WebAppInterface,
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
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

            addJavascriptInterface(webViewInterface, "KloudEvent")
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
