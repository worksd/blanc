package com.worksd.blanc.client

import android.app.Activity
import android.util.Log
import android.webkit.WebView
import com.google.gson.Gson
import com.worksd.blanc.data.KloudDialogInfo

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

fun WebView.onDialogConfirm(activity: Activity, dialogInfo: KloudDialogInfo) {
    activity.runOnUiThread {
        Log.d("WebAppInterface", "onDialogConfirm")
        this.loadUrl(
            createJavaScriptFunction(
                "onDialogConfirm",
                mapOf(
                    "id" to dialogInfo.id,
                    "type" to dialogInfo.type,
                    "route" to dialogInfo.route,
                    "hideForeverMessage" to dialogInfo.hideForeverMessage,
                    "imageUrl" to dialogInfo.imageUrl,
                    "imageRatio" to dialogInfo.imageRatio,
                    "title" to dialogInfo.title,
                    "message" to dialogInfo.message,
                    "ctaButtonText" to dialogInfo.ctaButtonText,
                )
            )
        )
    }
}

fun WebView.onHideDialog(activity: Activity, dialogId: String, clicked: Boolean) {
    activity.runOnUiThread {
        Log.d("WebAppInterface", "onHideDialogConfirm")
        this.loadUrl(
            createJavaScriptFunction(
                "onHideDialogConfirm",
                mapOf("id" to dialogId, "clicked" to clicked)
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