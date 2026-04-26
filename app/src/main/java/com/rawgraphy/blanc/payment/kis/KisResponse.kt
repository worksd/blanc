package com.rawgraphy.blanc.payment.kis

import android.content.Intent
import com.google.gson.JsonObject

/**
 * KIS-ANDAGT가 돌려준 Intent의 extras를 JS로 넘길 JSON으로 변환.
 *
 * - Agent가 넘긴 extras는 전부 top-level로 전달 (key는 그대로 outXxx 유지).
 * - 편의 필드 `success`(replyCode=="0000") / `canceled`(결과 취소 여부) 추가.
 * - 사용자가 Agent 화면에서 취소(RESULT_CANCELED) 시 replyCode가 없을 수 있어 별도 플래그.
 */
object KisResponse {

    fun fromActivityResult(resultCode: Int, data: Intent?): JsonObject {
        val json = JsonObject()
        val extras = data?.extras

        if (extras != null) {
            for (key in extras.keySet()) {
                val value = extras.get(key) ?: continue
                when (value) {
                    is String -> json.addProperty(key, value)
                    is Boolean -> json.addProperty(key, value)
                    is Int -> json.addProperty(key, value)
                    is Long -> json.addProperty(key, value)
                    is Float -> json.addProperty(key, value)
                    is Double -> json.addProperty(key, value)
                    else -> json.addProperty(key, value.toString())
                }
            }
        }

        val replyCode = json.get("outReplyCode")?.takeIf { it.isJsonPrimitive }?.asString
        val canceled = resultCode == android.app.Activity.RESULT_CANCELED && replyCode.isNullOrBlank()
        json.addProperty(
            "success",
            replyCode == KisConstants.REPLY_CODE_SUCCESS ||
                replyCode == KisConstants.REPLY_CODE_PAYCO_QUERY_SUCCESS
        )
        json.addProperty("canceled", canceled)
        json.addProperty("resultCode", resultCode)
        return json
    }
}
