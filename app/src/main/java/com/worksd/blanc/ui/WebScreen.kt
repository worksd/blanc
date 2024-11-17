package com.worksd.blanc.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.worksd.blanc.client.CustomWebViewClient
import com.worksd.blanc.EventReceiver
import com.worksd.blanc.client.WebAppInterface

@SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
@Composable
fun WebScreen(
    route: String,
    webInterface: EventReceiver,
    client: CustomWebViewClient,
) {
    val context = LocalContext.current
    val webAppInterface = remember { WebAppInterface(webInterface) }

    val webView = remember {
        WebView(context).apply {

            this.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            setBackgroundColor(resources.getColor(android.R.color.black, null))
            webViewClient = client
            settings.javaScriptEnabled = true
            settings.allowContentAccess = true
            settings.domStorageEnabled = true
            addJavascriptInterface(webAppInterface, "KloudEvent")
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    Log.d("WebAppInterface", "onConsoleMessage: ${consoleMessage?.message()}")
                    return super.onConsoleMessage(consoleMessage)
                }
            }
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
