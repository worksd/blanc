package com.rawgraphy.blanc.payment.kis

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject

/**
 * WebViewFragment에서 생성해 보관. KIS-ANDAGT Activity를 띄우고 결과를 콜백으로 돌려준다.
 *
 * 호출 시점: Fragment 생성 시점(onAttach 이전)에 초기화돼야 `registerForActivityResult`가 유효함.
 */
class KisAgentLauncher(
    fragment: Fragment,
    private val onResult: (JsonObject) -> Unit,
) {
    private val launcher = fragment.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val response = KisResponse.fromActivityResult(result.resultCode, result.data)
        logResult(result.resultCode, response)
        onResult(response)
    }

    fun launch(commandJson: String) {
        Log.i(TAG, "▶ launch() requested")
        Log.i(TAG, "  command: $commandJson")
        try {
            val intent = KisRequest.toIntent(commandJson)
            logIntent(intent)
            launcher.launch(intent)
            Log.i(TAG, "  ActivityResultLauncher.launch() called — waiting for Agent")
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "  KIS-ANDAGT not installed", e)
            val err = agentNotInstalledError()
            logResult(-1, err)
            onResult(err)
        } catch (e: Exception) {
            Log.e(TAG, "  KIS request failed: $commandJson", e)
            val err = unknownError(e.message ?: "unknown")
            logResult(-1, err)
            onResult(err)
        }
    }

    private fun logIntent(intent: Intent) {
        Log.i(TAG, "  intent.action=${intent.action}")
        Log.i(TAG, "  intent.package=${intent.`package`}")
        val extras = intent.extras
        if (extras != null) {
            for (key in extras.keySet()) {
                @Suppress("DEPRECATION")
                Log.i(TAG, "    extra[$key]=${extras.get(key)}")
            }
        }
    }

    private fun logResult(resultCode: Int, response: JsonObject) {
        Log.i(TAG, "◀ onKisPaymentResult resultCode=$resultCode")
        val success = response.get("success")?.asBoolean ?: false
        val canceled = response.get("canceled")?.asBoolean ?: false
        val replyCode = response.get("outReplyCode")?.asString
        val replyMsg = response.get("outReplyMsg1")?.asString
        val authNo = response.get("outAuthNo")?.asString
        val authDate = response.get("outAuthDate")?.asString
        val totAmt = response.get("outTotAmt")?.asString
        val cardNo = response.get("outCardNo")?.asString
        val accepter = response.get("outAccepterName")?.asString
        val tranCode = response.get("outTranCode")?.asString
        val customerUuid = response.get("outCustomerUuid")?.asString

        Log.i(TAG, "  success=$success  canceled=$canceled")
        Log.i(TAG, "  outReplyCode=$replyCode  outReplyMsg1=$replyMsg")
        if (!authNo.isNullOrBlank()) Log.i(TAG, "  outAuthNo=$authNo  outAuthDate=$authDate")
        if (!totAmt.isNullOrBlank()) Log.i(TAG, "  outTotAmt=$totAmt")
        if (!cardNo.isNullOrBlank()) Log.i(TAG, "  outCardNo=$cardNo  outAccepterName=$accepter")
        if (!tranCode.isNullOrBlank()) Log.i(TAG, "  outTranCode=$tranCode")
        if (!customerUuid.isNullOrBlank()) Log.i(TAG, "  outCustomerUuid=$customerUuid")
        Log.i(TAG, "  raw=$response")
    }

    private fun agentNotInstalledError(): JsonObject = JsonObject().apply {
        addProperty("success", false)
        addProperty("canceled", false)
        addProperty("outReplyCode", "E000")
        addProperty("outReplyMsg1", "KIS-ANDAGT 미설치")
    }

    private fun unknownError(message: String): JsonObject = JsonObject().apply {
        addProperty("success", false)
        addProperty("canceled", false)
        addProperty("outReplyCode", "E001")
        addProperty("outReplyMsg1", message)
    }

    companion object {
        private const val TAG = "KisPayment"
    }
}
