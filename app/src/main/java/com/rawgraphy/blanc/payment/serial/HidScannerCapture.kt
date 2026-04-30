package com.rawgraphy.blanc.payment.serial

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import com.google.gson.JsonObject

/**
 * HID 키보드 모드 QR/바코드 스캐너 캡처.
 *
 * 디바이스 펌웨어가 디코드 후 InputDispatcher 로 KEY 이벤트를 흘려보내는 케이스
 * (Newland Auto-ID BUS_V2 등). Activity.dispatchKeyEvent 에서 [dispatch] 를 가장 먼저
 * 호출 → 스캐너 디바이스에서 온 키만 가로채 버퍼링하고, 일정 시간 idle 또는 Enter 가
 * 들어오면 누적 문자열을 onScan 으로 넘김.
 *
 * 사람이 외부 키보드로 입력하는 경우와 구분하기 위해 device.name 패턴으로 필터링.
 */
object HidScannerCapture {
    private const val TAG = "HidScannerCapture"

    /** 스캐너로 간주할 InputDevice 이름 (lowercase 부분 일치). */
    private val SCANNER_NAME_PATTERNS = listOf(
        "newland", "auto-id", "bus_v2",
        "honeywell", "zebra", "symbol",
        "datalogic", "generalscan", "scanner",
        "barcode",
    )

    /** 마지막 키 이후 이만큼 더 안 들어오면 스캔 완료로 간주. */
    private const val IDLE_FLUSH_MS = 80L

    private val handler = Handler(Looper.getMainLooper())
    private val buffer = StringBuilder()

    @Volatile
    private var listener: ((JsonObject) -> Unit)? = null

    private val lock = Any()
    private val flushRunnable = Runnable { flush() }

    fun start(onEvent: (JsonObject) -> Unit) {
        Log.i(TAG, "▶ start()")
        synchronized(lock) {
            handler.removeCallbacks(flushRunnable)
            buffer.setLength(0)
            listener = onEvent
        }
        onEvent(JsonObject().apply { addProperty("event", "start") })
    }

    fun stop(onEvent: (JsonObject) -> Unit = {}) {
        Log.i(TAG, "▶ stop()")
        synchronized(lock) {
            handler.removeCallbacks(flushRunnable)
            buffer.setLength(0)
            listener = null
        }
        onEvent(JsonObject().apply { addProperty("event", "stopped") })
    }

    /**
     * Activity.dispatchKeyEvent 에서 호출. 스캐너 키이면 소비하고 true 반환.
     * 그 외는 false → 기본 처리에 위임.
     */
    fun dispatch(event: KeyEvent): Boolean {
        if (listener == null) return false
        if (!isScannerDevice(event)) return false

        // 스캐너에서 온 모든 이벤트는 항상 소비 — UP/DOWN 모두.
        if (event.action != KeyEvent.ACTION_DOWN) return true

        val keyCode = event.keyCode
        if (keyCode == KeyEvent.KEYCODE_ENTER ||
            keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER ||
            keyCode == KeyEvent.KEYCODE_TAB
        ) {
            handler.removeCallbacks(flushRunnable)
            flush()
            return true
        }

        val ch = event.unicodeChar
        if (ch != 0) {
            synchronized(lock) {
                buffer.append(ch.toChar())
            }
            handler.removeCallbacks(flushRunnable)
            handler.postDelayed(flushRunnable, IDLE_FLUSH_MS)
        }
        return true
    }

    private fun isScannerDevice(event: KeyEvent): Boolean {
        val name = event.device?.name?.lowercase() ?: return false
        return SCANNER_NAME_PATTERNS.any { name.contains(it) }
    }

    private fun flush() {
        val data: String
        val l: ((JsonObject) -> Unit)?
        synchronized(lock) {
            data = buffer.toString()
            buffer.setLength(0)
            l = listener
        }
        if (data.isEmpty() || l == null) return
        Log.i(TAG, "scan flushed (${data.length} chars): $data")
        l(JsonObject().apply {
            addProperty("event", "result")
            addProperty("success", true)
            addProperty("data", data)
            addProperty("source", "hid")
        })
    }
}
