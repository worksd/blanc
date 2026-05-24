package com.rawgraphy.blanc.client

import android.app.Activity
import android.util.Log
import android.webkit.WebView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rawgraphy.blanc.data.KloudDialogInfo

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
                    "customData" to dialogInfo.customData,
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

fun WebView.onCameraPermissionResult(activity: Activity, granted: Boolean) {
    activity.runOnUiThread {
        Log.d("WebAppInterface", "onCameraPermissionResult")
        this.loadUrl(
            createJavaScriptFunction(
                "onCameraPermissionResult",
                mapOf("granted" to granted)
            )
        )
    }
}

fun WebView.onKisPaymentResult(activity: Activity, result: JsonObject) {
    activity.runOnUiThread {
        Log.d("WebAppInterface", "onKisPaymentResult: $result")
        this.loadUrl("javascript:onKisPaymentResult($result)")
    }
}

fun WebView.onKisTransactionQueryResult(activity: Activity, result: JsonObject) {
    activity.runOnUiThread {
        Log.d("WebAppInterface", "onKisTransactionQueryResult: $result")
        this.loadUrl("javascript:onKisTransactionQueryResult($result)")
    }
}

fun WebView.onKioskModeResult(activity: Activity, state: com.rawgraphy.blanc.util.KioskState) {
    activity.runOnUiThread {
        val json = Gson().toJson(state)
        Log.d("WebAppInterface", "onKioskModeResult: $json")
        this.loadUrl("javascript:onKioskModeResult($json)")
    }
}

fun WebView.onSerialPrintResult(activity: Activity, result: JsonObject) {
    activity.runOnUiThread {
        Log.d("WebAppInterface", "onSerialPrintResult: $result")
        this.loadUrl("javascript:onSerialPrintResult($result)")
    }
}

fun WebView.onQrScanResult(activity: Activity, result: JsonObject) {
    activity.runOnUiThread {
        Log.d("WebAppInterface", "onQrScanResult: $result")
        this.loadUrl("javascript:onQrScanResult($result)")
    }
}

fun WebView.onFcmTokenReceived(activity: Activity, fcmToken: String, udid: String) {
    activity.runOnUiThread {
        Log.d("WebAppInterface", "onFcmTokenReceived")
        this.loadUrl(
            createJavaScriptFunction(
                "onFcmTokenReceived",
                mapOf("fcmToken" to fcmToken, "udid" to udid)
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