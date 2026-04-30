package com.rawgraphy.blanc.payment.serial

import android.content.Context
import android.util.Log
import com.centerm.oversea.libpos.dev.pos.Pos
import com.centerm.oversea.libpos.listener.ResultCallback
import com.centerm.oversea.libpos.model.ActionResult
import com.centerm.oversea.libposaidl.aidl.model.TextPrintData
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Centerm POS SDK 기반 RS-232 프린터 직접 제어 (KIS-ANDAGT 우회).
 *
 * 첫 호출에서 [Pos.init] 비동기 바인딩 → 성공 시 후속 호출은 즉시 처리.
 * 동시 호출은 큐잉 후 init 완료 시 일괄 실행.
 *
 * 입력 JSON 스키마:
 * {
 *   "lines": [
 *     { "align":"L"|"C"|"R", "text":"...", "bold":bool, "scaleWidth":bool, "scaleHeight":bool },
 *     { "blank": <N> }
 *   ],
 *   "autoCut": true,        // optional, default true
 *   "cutType": 1            // 0=Full, 1=Partial, default 1
 * }
 */
object CentermPrinter {
    private const val TAG = "CentermPrinter"

    // Centerm PrinterConstant.Align
    private const val ALIGN_LEFT = 1
    private const val ALIGN_CENTER = 2
    private const val ALIGN_RIGHT = 3

    fun print(context: Context, commandJson: String, onResult: (JsonObject) -> Unit) {
        Log.i(TAG, "▶ print() requested")
        Log.i(TAG, "  command: $commandJson")
        CentermPos.ensureInitialized(context.applicationContext) { initOk ->
            if (!initOk) {
                val reason = CentermPos.lastInitFailure ?: "unknown"
                onResult(error("init_failed", "Pos SDK init failed: $reason"))
                return@ensureInitialized
            }
            doPrint(commandJson, onResult)
        }
    }

    private fun doPrint(commandJson: String, onResult: (JsonObject) -> Unit) {
        try {
            val root = JsonParser.parseString(commandJson).asJsonObject
            val lines = root.get("lines")?.takeIf { it.isJsonArray }?.asJsonArray
            if (lines == null || lines.size() == 0) {
                onResult(error("empty_lines", "lines is empty"))
                return
            }
            val autoCut = root.get("autoCut")?.takeIf { it.isJsonPrimitive }?.asBoolean ?: true
            val cutType = root.get("cutType")?.takeIf { it.isJsonPrimitive }?.asInt ?: 1

            val printer = Pos.get().printer
            printer.setAutoCut(autoCut, cutType)
            Log.i(TAG, "  setAutoCut($autoCut, $cutType)")

            for (el in lines) {
                if (!el.isJsonObject) continue
                val o = el.asJsonObject
                val blank = o.get("blank")?.takeIf { it.isJsonPrimitive }?.asInt ?: 0
                if (blank > 0) {
                    printer.addText(TextPrintData("\n".repeat(blank)))
                    continue
                }
                val text = o.get("text")?.asString.orEmpty()
                val align = when (o.get("align")?.asString?.uppercase()) {
                    "C" -> ALIGN_CENTER
                    "R" -> ALIGN_RIGHT
                    else -> ALIGN_LEFT
                }
                val bold = o.get("bold")?.asBoolean ?: false
                val scaleW = o.get("scaleWidth")?.asBoolean ?: false
                val scaleH = o.get("scaleHeight")?.asBoolean ?: false
                printer.addText(
                    TextPrintData(text + "\n", align, false, bold, scaleW, scaleH, 0, 0)
                )
            }
            Log.i(TAG, "  ${lines.size()} line(s) queued, calling print()")

            printer.print(object : ResultCallback<Void> {
                override fun onStart() {
                    Log.i(TAG, "  printer.print() onStart")
                }
                override fun onResult(result: ActionResult<Void>) {
                    Log.i(
                        TAG,
                        "◀ printer.print() onResult: code=${result.resultCode}" +
                            " success=${result.isSuccessful} canceled=${result.isCanceled}"
                    )
                    result.throwable?.let { Log.e(TAG, "  throwable", it) }
                    onResult(resultJson(result))
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "  doPrint threw", e)
            onResult(error("exception", e.message ?: e.javaClass.simpleName))
        }
    }

    private fun resultJson(result: ActionResult<Void>): JsonObject = JsonObject().apply {
        addProperty("success", result.isSuccessful)
        addProperty("canceled", result.isCanceled)
        addProperty("resultCode", result.resultCode)
        result.throwable?.message?.let { addProperty("error", it) }
    }

    private fun error(code: String, msg: String): JsonObject = JsonObject().apply {
        addProperty("success", false)
        addProperty("canceled", false)
        addProperty("error", "$code: $msg")
    }
}
