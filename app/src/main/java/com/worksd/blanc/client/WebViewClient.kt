package com.worksd.blanc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient


class CustomWebViewClient(val context: Context) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        Log.d("WebAppInterface", "onPageStarted: $url")
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)

        Log.d("WebAppInterface", "onReceivedError: ${error?.description}")
    }
}

class WebAppInterface(private val activity: Activity) {

    private val TAG = "WebAppInterface"

    @JavascriptInterface
    fun navigate(screen: String) {
        Log.d(TAG, "navigate: $screen")
        val intent = Intent(activity, WebViewActivity::class.java)
        intent.putExtra(EXTRAS_ROUTE, screen)
        activity.startActivity(intent)
        activity.finish()
    }

    @JavascriptInterface
    fun navigateMain(bootInfo: String) {
        Log.d(TAG, "navigateMain: $bootInfo")
        val intent = Intent(activity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtras(Bundle().apply {
            putString(EXTRAS_BOOT_INFO, bootInfo)
        })
        activity.startActivity(intent)
    }
}