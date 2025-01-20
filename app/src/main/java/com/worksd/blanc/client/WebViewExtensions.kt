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

fun WebView.onPaymentSuccess(activity: Activity, transactionId: String, paymentId: String) {
    activity.runOnUiThread {
        Log.d("WebAppInterface", "onPaymentSuccess")
        this.loadUrl(
            createJavaScriptFunction(
                "onPaymentSuccess",
                mapOf("transactionId" to transactionId, "paymentId" to paymentId)
            )
        )
    }
}

fun WebView.onDialogConfirm(activity: Activity, id: String) {
    activity.runOnUiThread {
        Log.d("WebAppInterface", "onDialogConfirm")
        this.loadUrl(
            createJavaScriptFunction(
                "onDialogConfirm",
                mapOf("id" to id)
            )
        )
    }
}

fun WebView.onErrorInvoked(activity: Activity, code: String) {
    activity.runOnUiThread {
        Log.d("WebAppInterface", "onErrorInvoked")
        this.loadUrl(
            createJavaScriptFunction(
                "onErrorInvoked",
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