package com.worksd.blanc.client

import android.app.Activity
import android.util.Log
import android.webkit.WebView
import com.google.gson.Gson

fun WebView.onKakaoLoginSuccess(activity: Activity, code: String) {
    activity.runOnUiThread {
        Log.d("WebAppInterface", "onKakaoLoginSuccess")
        this.loadUrl(
            createJavaScriptFunction(
                "onKakaoLoginSuccess",
                mapOf("code" to code)
            )
        )
    }
}

fun WebView.onGoogleLoginSuccess(activity: Activity, code: String) {
    activity.runOnUiThread {
        Log.d("WebAppInterface", "onGoogleLoginSuccess")
        this.loadUrl(
            createJavaScriptFunction(
                "onGoogleLoginSuccess",
                mapOf("code" to code)
            )
        )
    }
}

private fun createJavaScriptFunction(functionName: String, parameterMap: Any?): String {
    val paramJsonString = parameterMap?.let {
        Gson().toJson(parameterMap)
    } ?: ""
    return "javascript:$functionName($paramJsonString)"
}