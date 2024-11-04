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
import android.widget.Toast
import java.util.Date


class CustomWebViewClient(val context: Context): WebViewClient(){

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

    // JavaScript에서 호출할 메서드
    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun navigate(screen: String) {
        Log.d(TAG, "navigate: $screen")

        if (screen == "main") {
            val intent = Intent(activity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity.startActivity(intent)
        } else {
            val intent = Intent(activity, SchemeWebViewActivity::class.java)
            intent.putExtras(Bundle().apply {
                putString(SCHEME_WEB_VIEW_ROUTE, screen)
            })
            activity.startActivity(intent)
            activity.finish()
        }
    }
}