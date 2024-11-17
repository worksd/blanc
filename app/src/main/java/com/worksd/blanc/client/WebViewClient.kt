package com.worksd.blanc.client

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import com.worksd.blanc.EventReceiver
import com.worksd.blanc.WebViewListener


class CustomWebViewClient(val context: Context, val listener: WebViewListener) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        Log.d("WebAppInterface", "onPageStarted: $url")

        listener.onConnectSuccess()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        Log.d("WebAppInterface", "onReceivedError: ${error?.description}")
        listener.onConnectFail()
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        super.onReceivedHttpError(view, request, errorResponse)

        Log.d("WebAppInterface", "onReceivedError:")
        listener.onConnectFail()
    }
}

class WebAppInterface(val receiver: EventReceiver) {

    private val TAG = "WebAppInterface"

    @JavascriptInterface
    fun navigate(screen: String) {
//        Log.d(TAG, "navigate: $screen")
//        val intent = Intent(activity, WebViewActivity::class.java)
//        intent.putExtra(EXTRAS_ROUTE, screen)
//        activity.startActivity(intent)
//        activity.finish()
        receiver.navigate(screen)
    }

    @JavascriptInterface
    fun sendBootInfo(bootInfo: String) {
        Log.d("WebAppInterface", "sendBootInfo: $bootInfo")
        receiver.fetchBootInfo(bootInfo)
    }

    @JavascriptInterface
    fun onSplashStarted() {
        Log.d("WebAppInterface", "onSplashStarted")
    }

}


private fun createJavaScriptFunction(functionName: String, parameterMap: Any?): String {
    val paramJsonString = parameterMap?.let {
        Gson().toJson(parameterMap)
    } ?: ""
    return "javascript:$functionName($paramJsonString)"
}


@JavascriptInterface
fun WebView.onSplashStarted() {
    Log.d("WebAppInterface", "onSplashStarted")
    this.loadUrl(createJavaScriptFunction("onSplashStarted", null))
}