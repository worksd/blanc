package com.rawgraphy.blanc.payment.serial

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import kotlin.concurrent.thread

/**
 * USB-Serial 프린터(`/dev/ttyUSBx`)에 ESC/POS 명령을 직접 써넣는 단순 프린터.
 * KIS-ANDAGT, Centerm SDK 등 미들웨어를 모두 우회 — 시스템 서비스 의존 없음.
 *
 * 입력 JSON:
 * {
 *   "lines": [
 *     { "align":"L|C|R", "text":"...", "bold":bool },
 *     { "blank": <N> }
 *   ],
 *   "devicePath": "/dev/ttyUSB2",  // optional, default "/dev/ttyUSB2"
 *   "baud": 9600,                  // optional, default 9600 (stty로 설정 시도)
 *   "cut": true,                   // optional, default true
 *   "raw": "..."                   // optional, 있으면 lines/cut 무시하고 이 문자열만 그대로 송신 (디버그용)
 * }
 *
 * 동작 조건: 앱 uid가 해당 tty 디바이스에 read/write 권한을 가져야 함.
 */
object SerialPrinter {
    private const val TAG = "SerialPrinter"
    private const val DEFAULT_DEVICE = "/dev/ttyUSB2"
    private val EUC_KR: Charset = Charset.forName("EUC-KR")

    fun print(commandJson: String, onResult: (JsonObject) -> Unit) {
        Log.i(TAG, "▶ print() requested")
        Log.i(TAG, "  command: $commandJson")
        thread(name = "SerialPrinter", isDaemon = true) {
            val root = runCatching { JsonParser.parseString(commandJson).asJsonObject }.getOrNull()
            when {
                root?.get("scan")?.takeIf { it.isJsonPrimitive }?.asBoolean == true ->
                    onResult(runScan())
                root?.get("query")?.takeIf { it.isJsonPrimitive }?.asBoolean == true ->
                    onResult(runQuery(
                        root.get("devicePath")?.takeIf { it.isJsonPrimitive }?.asString ?: DEFAULT_DEVICE,
                        root.get("baud")?.takeIf { it.isJsonPrimitive }?.asInt ?: 9600
                    ))
                else -> onResult(doPrint(commandJson))
            }
        }
    }

    /**
     * 진단용 — 프린터에 정보 요청 명령을 차례로 쏘고 응답을 dump.
     * 모델명, 펌웨어 버전, 제조사, 상태 등을 hex와 ASCII로 같이 보여줌.
     */
    private fun runQuery(devicePath: String, baud: Int): JsonObject {
        configureSerial(devicePath, baud)
        val queries = listOf(
            "GS I 1 (model id)"     to byteArrayOf(0x1D, 0x49, 0x01),
            "GS I 2 (type id)"      to byteArrayOf(0x1D, 0x49, 0x02),
            "GS I 3 (rom version)"  to byteArrayOf(0x1D, 0x49, 0x03),
            "GS I 49 (info)"        to byteArrayOf(0x1D, 0x49, 0x31),
            "GS I 65 (firmware)"    to byteArrayOf(0x1D, 0x49, 0x41),
            "GS I 66 (maker)"       to byteArrayOf(0x1D, 0x49, 0x42),
            "GS I 67 (model name)"  to byteArrayOf(0x1D, 0x49, 0x43),
            "GS I 68 (serial)"      to byteArrayOf(0x1D, 0x49, 0x44),
            "DLE EOT 1 (status)"    to byteArrayOf(0x10, 0x04, 0x01),
            "DLE EOT 2 (offline)"   to byteArrayOf(0x10, 0x04, 0x02),
            "DLE EOT 3 (error)"     to byteArrayOf(0x10, 0x04, 0x03),
            "DLE EOT 4 (paper)"     to byteArrayOf(0x10, 0x04, 0x04),
        )
        val results = JsonArray()
        val fis = try {
            FileInputStream(devicePath)
        } catch (e: Exception) {
            return error("open_read_failed", "${e.javaClass.simpleName}: ${e.message ?: ""}")
        }
        try {
            FileOutputStream(devicePath).use { fos ->
                for ((name, cmd) in queries) {
                    fos.write(cmd)
                    fos.flush()
                    Thread.sleep(400)
                    val resp = readAvailable(fis, maxBytes = 256)
                    val hex = resp.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }
                    val ascii = resp.toString(EUC_KR).replace(Regex("[\\u0000-\\u001F]"), ".")
                    Log.i(TAG, "  query $name → len=${resp.size} hex=[$hex] ascii=[$ascii]")
                    val r = JsonObject().apply {
                        addProperty("query", name)
                        addProperty("len", resp.size)
                        addProperty("hex", hex)
                        addProperty("ascii", ascii)
                    }
                    results.add(r)
                }
            }
        } finally {
            runCatching { fis.close() }
        }
        return JsonObject().apply {
            addProperty("success", true)
            addProperty("device", devicePath)
            addProperty("baud", baud)
            add("queries", results)
        }
    }

    private fun readAvailable(fis: FileInputStream, maxBytes: Int): ByteArray {
        val avail = try { fis.available() } catch (_: Exception) { 0 }
        if (avail <= 0) return ByteArray(0)
        val buf = ByteArray(minOf(avail, maxBytes))
        val n = try { fis.read(buf) } catch (_: Exception) { 0 }
        return if (n <= 0) ByteArray(0) else buf.copyOf(n)
    }

    /**
     * 진단용 — 자주 쓰이는 baud × tty 경로 조합을 모두 시도. 각 조합마다 식별 가능한
     * 평문을 1초 간격으로 송신. 사용자는 종이를 보고 어느 조합에서 글자가 나오는지 확인.
     */
    private fun runScan(): JsonObject {
        val paths = listOf("/dev/ttyUSB0", "/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyS0", "/dev/ttyS1")
        val bauds = listOf(9600, 19200, 38400, 57600, 115200)
        val tried = mutableListOf<String>()

        for (path in paths) {
            for (baud in bauds) {
                val tag = "PATH=$path BAUD=$baud"
                try {
                    configureSerial(path, baud)
                    val msg = "$tag\n\n\n\n\n"
                    FileOutputStream(path).use { it.write(msg.toByteArray(EUC_KR)); it.flush() }
                    Log.i(TAG, "  scan ✓ $tag")
                    tried.add(tag)
                    Thread.sleep(1000)
                } catch (e: Exception) {
                    Log.i(TAG, "  scan ✗ $tag (${e.javaClass.simpleName}: ${e.message})")
                }
            }
        }
        return JsonObject().apply {
            addProperty("success", true)
            addProperty("scanned", tried.size)
            addProperty("note", "프린터에서 출력된 라인의 PATH와 BAUD 값이 정답입니다")
        }
    }

    private fun doPrint(commandJson: String): JsonObject {
        return try {
            val root = JsonParser.parseString(commandJson).asJsonObject
            val devicePath = root.get("devicePath")?.takeIf { it.isJsonPrimitive }?.asString
                ?: DEFAULT_DEVICE
            val baud = root.get("baud")?.takeIf { it.isJsonPrimitive }?.asInt ?: 9600
            val raw = root.get("raw")?.takeIf { it.isJsonPrimitive }?.asString

            configureSerial(devicePath, baud)

            val bytes: ByteArray = if (raw != null) {
                val noInit = root.get("noInit")?.takeIf { it.isJsonPrimitive }?.asBoolean ?: false
                Log.i(TAG, "  RAW mode noInit=$noInit")
                val body = raw.toByteArray(EUC_KR)
                if (noInit) {
                    body
                } else {
                    // POSBANK A8R-B 검증 — ESC S(1B 53)는 일부 펌웨어에서 텍스트를 흡수해
                    // 빼고, 안전하게 print mode + line spacing만 디폴트로 강제.
                    val prefix = byteArrayOf(
                        0x1B, 0x40,        // ESC @ - initialize
                        0x1B, 0x21, 0x00,  // ESC ! 0 - normal print mode (cancel bold/double/underline)
                        0x1B, 0x32,        // ESC 2 - default line spacing
                        0x1C, 0x2E         // FS .  - cancel kanji mode
                    )
                    prefix + body
                }
            } else {
                val lines = root.get("lines")?.takeIf { it.isJsonArray }?.asJsonArray
                if (lines == null || lines.size() == 0) {
                    return error("empty_lines", "lines or raw required")
                }
                val cut = root.get("cut")?.takeIf { it.isJsonPrimitive }?.asBoolean ?: true
                buildEscPos(lines, cut)
            }

            Log.i(TAG, "  device=$devicePath  baud=$baud  bytes=${bytes.size}")
            FileOutputStream(devicePath).use { fos ->
                fos.write(bytes)
                fos.flush()
            }
            Log.i(TAG, "◀ wrote ${bytes.size} bytes to $devicePath")
            success(bytes.size, devicePath)
        } catch (e: Exception) {
            Log.e(TAG, "  print failed", e)
            error("io_failed", "${e.javaClass.simpleName}: ${e.message ?: ""}")
        }
    }

    /**
     * stty 로 시리얼 라인 설정. 단말에 stty 가 없거나 권한이 없으면 조용히 실패.
     * 성공 시: <baud> 8N1 raw mode, 모든 flow control off.
     */
    private fun configureSerial(devicePath: String, baud: Int) {
        val cmd = "stty -F $devicePath $baud cs8 -cstopb -parenb -ixon -ixoff -crtscts raw -echo"
        try {
            val proc = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            val ok = proc.waitFor()
            Log.i(TAG, "  stty exit=$ok ($cmd)")
        } catch (e: Exception) {
            Log.w(TAG, "  stty failed: ${e.message} — continuing with kernel defaults")
        }
    }

    private fun buildEscPos(lines: JsonArray, cut: Boolean): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(byteArrayOf(0x1B, 0x40))         // ESC @ — initialize
        out.write(byteArrayOf(0x1B, 0x21, 0x00))   // ESC ! 0 — normal print mode
        out.write(byteArrayOf(0x1B, 0x32))         // ESC 2 — default line spacing
        out.write(byteArrayOf(0x1C, 0x2E))         // FS . — cancel kanji mode

        for (el in lines) {
            if (!el.isJsonObject) continue
            val o = el.asJsonObject
            val blank = o.get("blank")?.takeIf { it.isJsonPrimitive }?.asInt ?: 0
            if (blank > 0) {
                repeat(blank) { out.write(byteArrayOf(0x0A)) }
                continue
            }
            val alignByte: Byte = when (o.get("align")?.asString?.uppercase()) {
                "C" -> 0x01
                "R" -> 0x02
                else -> 0x00
            }
            out.write(byteArrayOf(0x1B, 0x61, alignByte))

            val qr = o.get("qr")?.takeIf { it.isJsonPrimitive }?.asString
            if (!qr.isNullOrEmpty()) {
                writeQr(out, qr, o)
                continue
            }

            val bold = o.get("bold")?.asBoolean ?: false
            if (bold) out.write(byteArrayOf(0x1B, 0x21, 0x08))   // emphasized
            val text = o.get("text")?.asString.orEmpty()
            out.write(text.toByteArray(EUC_KR))
            if (bold) out.write(byteArrayOf(0x1B, 0x21, 0x00))

            out.write(byteArrayOf(0x0A))
        }

        // 마지막 텍스트가 컷 라인을 넘어가지 않도록 충분히 피드
        out.write(byteArrayOf(0x1B, 0x64, 0x05))   // ESC d 5 — 5라인 피드

        if (cut) {
            out.write(byteArrayOf(0x1D, 0x56, 0x42, 0x00))   // GS V B 0 — full cut
        }
        return out.toByteArray()
    }

    /**
     * ESC/POS 표준 QR 시퀀스 (GS ( k Function 165~169) — Bixolon/Sewoo/Star/Custom 호환.
     * 정렬은 호출 측이 이미 emit. 여기서는 모델/사이즈/EC/데이터/프린트 순으로 작성하고 LF 마무리.
     */
    private fun writeQr(out: ByteArrayOutputStream, qr: String, o: JsonObject) {
        val size = (o.get("size")?.takeIf { it.isJsonPrimitive }?.asInt ?: 6).coerceIn(1, 16)
        val ec: Byte = when (o.get("ec")?.asString?.uppercase()) {
            "L" -> 0x30
            "Q" -> 0x32
            "H" -> 0x33
            else -> 0x31    // M
        }

        // Model 2
        out.write(byteArrayOf(0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00))
        // Module size
        out.write(byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, size.toByte()))
        // Error correction level
        out.write(byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, ec))
        // Store data: pL pH = data.size + 3
        val data = qr.toByteArray(Charsets.UTF_8)
        val len = data.size + 3
        val pL = (len and 0xFF).toByte()
        val pH = ((len shr 8) and 0xFF).toByte()
        out.write(byteArrayOf(0x1D, 0x28, 0x6B, pL, pH, 0x31, 0x50, 0x30))
        out.write(data)
        // Print
        out.write(byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30))
        out.write(byteArrayOf(0x0A))
    }

    private fun success(byteCount: Int, devicePath: String): JsonObject = JsonObject().apply {
        addProperty("success", true)
        addProperty("canceled", false)
        addProperty("bytes", byteCount)
        addProperty("device", devicePath)
    }

    private fun error(code: String, msg: String): JsonObject = JsonObject().apply {
        addProperty("success", false)
        addProperty("canceled", false)
        addProperty("error", "$code: $msg")
    }
}
