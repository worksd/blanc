package com.rawgraphy.blanc.client

import android.graphics.Bitmap
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.rawgraphy.blanc.data.GoogleLoginConfiguration
import com.rawgraphy.blanc.data.KloudDialogInfo
import com.rawgraphy.blanc.data.RouteInfo


class CustomWebViewClient(private val listener: WebViewListener) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        Log.d("WebAppInterface", "onPageStarted: $url")

        listener.onConnectSuccess()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        listener.onPageFinished()
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
        Log.d("WebAppInterface", "screen = $screen")
        receiver.replace(screen)
    }

    @JavascriptInterface
    fun push(screen: String) {
        Log.d("WebAppInterface", "screen = $screen")
        val routeInfo = Gson().fromJson(screen, RouteInfo::class.java)
        receiver.push(routeInfo)
    }

    @JavascriptInterface
    fun fullSheet(screen: String) {
        Log.d("WebAppInterface", "screen = $screen")
        val routeInfo = Gson().fromJson(screen, RouteInfo::class.java)
        receiver.fullSheet(routeInfo)
    }

    @JavascriptInterface
    fun back() {
        receiver.back()
    }

    @JavascriptInterface
    fun clearAndPush(screen: String) {
        Log.d("WebAppInterface", "screen = $screen")
        val routeInfo = Gson().fromJson(screen, RouteInfo::class.java)
        receiver.pushAndAllClear(routeInfo)
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
    fun sendGoogleLogin(configuration: String) {
        val googleConfiguration = Gson().fromJson(configuration, GoogleLoginConfiguration::class.java)
        receiver.sendGoogleLogin(googleConfiguration)
    }

    @JavascriptInterface
    fun showDialog(input: String) {
        val dialogInfo = Gson().fromJson(input, KloudDialogInfo::class.java)
        receiver.showDialog(dialogInfo)
    }

    @JavascriptInterface
    fun showBottomSheet(route: String) {
        receiver.showBottomSheet(route)
    }

    @JavascriptInterface
    fun requestPayment(command: String) {
        receiver.requestPayment(command)
    }

    @JavascriptInterface
    fun registerDevice() {
        receiver.sendFcmToken()
    }

    @JavascriptInterface
    fun closeBottomSheet() {
        receiver.closeBottomSheet()
    }

    @JavascriptInterface
    fun changeWebEndpoint(endpoint: String) {
        receiver.changeWebEndpoint(endpoint)
    }

    @JavascriptInterface
    fun openExternalBrowser(url: String) {
        receiver.openExternalBrowser(url)
    }

    @JavascriptInterface
    fun refresh(endpoint: String) {
        receiver.refresh(endpoint)
    }
}