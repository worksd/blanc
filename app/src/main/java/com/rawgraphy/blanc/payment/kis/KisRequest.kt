package com.rawgraphy.blanc.payment.kis

import android.content.Intent
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.UUID

/**
 * JS에서 넘어온 JSON을 KIS-ANDAGT용 Intent로 변환.
 *
 * JS 측 계약: `kr.co.kisvan.andagent` 개발 가이드(inXxx)와 샘플(ResultType 등)의
 * 실제 extras 키를 그대로 JSON 키로 사용. 예:
 * { "inTranCode":"D1", "inTotAmt":"10000", "inTestMode":"Y", "inCustomerUuid":"..." }
 *
 * 네이티브는 값의 JSON 타입(string/number/bool)을 보고 적절한 putExtra 호출.
 * `ResultType`은 강제로 Activity 모드(0)로 고정 — Broadcast 모드는 응답 누락 위험이 있어 미사용.
 *
 * 인쇄(PR/BP)는 byte[]/String 처리가 필요해 `inPrintLines` 배열로 받아 거래종류에 맞게 변환:
 *  - 라인 객체: { "align":"L|C|R", "text":"...", "bold":true|false }  또는  { "blank": <N> }
 *  - PR + inSecondTranCode="0" (센텀 내장): `inPrintData`(String, 평문 + \n, 공백 패딩)
 *  - PR + inSecondTranCode="1" (외장 COM)  : `inPrtData`(byte[], Epson ESC/POS)
 *  - BP (CBP2200 내장)                     : `inPrtData`(byte[], Epson ESC/POS)
 */
object KisRequest {

    private val EUC_KR: Charset = Charset.forName("EUC-KR")
    private const val CENTERM_LINE_WIDTH = 48   // 센텀 한 줄 칸수 (ASCII 기준, 한글은 2칸)

    fun toIntent(commandJson: String): Intent {
        val root = JsonParser.parseString(commandJson).asJsonObject
        val tranCode = root.get("inTranCode")?.takeIf { it.isJsonPrimitive }?.asString
        val isPrintCommand = tranCode == "PR" || tranCode == "BP"

        if (!isPrintCommand) ensureCustomerUuid(root)
        if (isPrintCommand) ensurePrintDefaults(root)

        val intent = Intent(KisConstants.AGENT_INTENT_ACTION).apply {
            setPackage(KisConstants.AGENT_PACKAGE)
        }

        applyPrintData(root, intent)

        for ((key, value) in root.entrySet()) {
            if (key == KisConstants.EXTRA_RESULT_TYPE) continue
            if (value == null || value.isJsonNull) continue
            if (!value.isJsonPrimitive) continue

            val prim = value.asJsonPrimitive
            when {
                prim.isBoolean -> intent.putExtra(key, prim.asBoolean)
                prim.isNumber -> {
                    val n = prim.asNumber
                    if (n.toLong().toDouble() == n.toDouble()) {
                        intent.putExtra(key, n.toLong())
                    } else {
                        intent.putExtra(key, n.toDouble())
                    }
                }
                else -> intent.putExtra(key, prim.asString)
            }
        }

        intent.putExtra(KisConstants.EXTRA_RESULT_TYPE, KisConstants.RESULT_TYPE_ACTIVITY)
        return intent
    }

    private fun applyPrintData(root: JsonObject, intent: Intent) {
        val arr = root.remove("inPrintLines")?.takeIf { it.isJsonArray }?.asJsonArray ?: return
        val tranCode = root.get("inTranCode")?.takeIf { it.isJsonPrimitive }?.asString
        val secondTranCode = root.get("inSecondTranCode")?.takeIf { it.isJsonPrimitive }?.asString

        if (tranCode == "PR" && secondTranCode == "0") {
            intent.putExtra("inPrintData", buildCentermText(arr))
        } else {
            intent.putExtra("inPrtData", buildEscPosBytes(arr))
        }
    }

    private fun buildCentermText(arr: JsonArray): String {
        val sb = StringBuilder()
        for (el in arr) {
            if (!el.isJsonObject) continue
            val o = el.asJsonObject
            val blank = o.get("blank")?.takeIf { it.isJsonPrimitive }?.asInt ?: 0
            if (blank > 0) {
                repeat(blank) { sb.append('\n') }
                continue
            }
            val text = o.get("text")?.asString.orEmpty()
            val align = o.get("align")?.asString?.uppercase() ?: "L"
            sb.append(padLine(text, align)).append('\n')
        }
        return sb.toString()
    }

    private fun padLine(text: String, align: String): String {
        val width = visualWidth(text)
        if (width >= CENTERM_LINE_WIDTH) return text
        val space = CENTERM_LINE_WIDTH - width
        return when (align) {
            "C" -> " ".repeat(space / 2) + text
            "R" -> " ".repeat(space) + text
            else -> text
        }
    }

    private fun visualWidth(s: String): Int {
        var w = 0
        for (ch in s) w += if (ch.code > 127) 2 else 1
        return w
    }

    private fun buildEscPosBytes(arr: JsonArray): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(byteArrayOf(0x1B, 0x40))  // ESC @ - 프린터 초기화
        for (el in arr) {
            if (!el.isJsonObject) continue
            val o = el.asJsonObject
            val blank = o.get("blank")?.takeIf { it.isJsonPrimitive }?.asInt ?: 0
            if (blank > 0) {
                repeat(blank) { out.write(byteArrayOf(0x0D, 0x0A)) }
                continue
            }
            val align = when (o.get("align")?.asString?.uppercase()) {
                "C" -> byteArrayOf(0x1B, 0x61, 0x01)
                "R" -> byteArrayOf(0x1B, 0x61, 0x02)
                else -> byteArrayOf(0x1B, 0x61, 0x00)
            }
            out.write(align)

            val bold = o.get("bold")?.asBoolean ?: false
            if (bold) out.write(byteArrayOf(0x1B, 0x21, 0x20))
            val text = o.get("text")?.asString.orEmpty()
            out.write(text.toByteArray(EUC_KR))
            if (bold) out.write(byteArrayOf(0x1B, 0x21, 0x00))

            out.write(byteArrayOf(0x0D, 0x0A))
        }
        return out.toByteArray()
    }

    private fun ensureCustomerUuid(root: JsonObject) {
        val existing = root.get("inCustomerUuid")?.takeIf { it.isJsonPrimitive }?.asString
        if (existing.isNullOrBlank()) {
            root.addProperty("inCustomerUuid", UUID.randomUUID().toString())
        }
    }

    /** PR/BP 호출 시 샘플에서 항상 함께 보내는 필수 extras 기본값 주입. JS가 이미 보냈으면 보존. */
    private fun ensurePrintDefaults(root: JsonObject) {
        if (!root.has("inAutoCut")) root.addProperty("inAutoCut", true)
        if (!root.has("inCuttingSet")) root.addProperty("inCuttingSet", 1)  // 0=Full, 1=Partial
        if (!root.has("inConnectToCat")) root.addProperty("inConnectToCat", false)
    }
}
