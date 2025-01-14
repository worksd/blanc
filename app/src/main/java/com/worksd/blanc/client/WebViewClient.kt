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


class CustomWebViewClient(private val listener: WebViewListener) : WebViewClient() {

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
    fun replace(screen: String) {
        receiver.replace(screen)
    }

    @JavascriptInterface
    fun push(screen: String) {
        receiver.push(screen)
    }

    @JavascriptInterface
    fun back() {
        receiver.back()
    }

    @JavascriptInterface
    fun clearAndPush(screen: String) {
        receiver.pushAndAllClear(screen)
    }

    @JavascriptInterface
    fun navigateMain(bootInfo: String) {
        receiver.navigateMain(bootInfo)
    }

    @JavascriptInterface
    fun clearToken() {
        receiver.clearToken()
    }

    @JavascriptInterface
    fun showToast(message: String) {
        receiver.showToast(message)
    }

    @JavascriptInterface
    fun sendHapticFeedback() {
        receiver.sendHapticFeedback()
    }

    @JavascriptInterface
    fun sendKakaoLogin() {
        receiver.sendKakaoLogin()
    }

    @JavascriptInterface
    fun sendGoogleLogin() {
        receiver.sendGoogleLogin()
    }
}