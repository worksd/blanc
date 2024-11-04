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

        Toast.makeText(context, "Loading started", Toast.LENGTH_SHORT).show()
        Log.d("DORODORO", "GOOD")
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)

        Toast.makeText(context, "Error occurred", Toast.LENGTH_SHORT).show()
        Log.e("DORODORO", "Error: " + error?.description)
    }
}

class WebAppInterface(private val activity: Activity) {

    // JavaScript에서 호출할 메서드
    @JavascriptInterface
    fun showToast(message: String) {
        Log.d("DORODORO", "showToast: $message")
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun navigate(screen: String) {
        Log.d("DORODORO", "navigate: $screen")
        val intent = Intent(activity, SchemeWebViewActivity::class.java)
        val route = when(screen) {
            "Login" -> "login"
            else -> "Unknown"
        }
        intent.putExtras(Bundle().apply {
            putString(SCHEME_WEB_VIEW_ROUTE, route)
        })
        activity.startActivity(intent)
        activity.finish()
    }

    @JavascriptInterface
    fun getDataFromAndroid(): String {
        // 데이터를 반환하는 메서드
        return "보내자잇 ${Date().time}"
    }
}