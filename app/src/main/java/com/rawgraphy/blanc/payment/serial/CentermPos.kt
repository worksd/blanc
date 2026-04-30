package com.rawgraphy.blanc.payment.serial

import android.content.Context
import android.util.Log
import com.centerm.oversea.libpos.dev.pos.Pos
import com.centerm.oversea.libpos.listener.InitResultCallback
import com.centerm.oversea.libpos.model.ActionResult

/**
 * Centerm POS SDK 바인딩을 한 번만 수행하고 결과를 공유하는 싱글턴.
 * Printer / Scanner 가 동시에 초기화를 요청해도 init 레이스가 발생하지 않도록 큐잉.
 */
object CentermPos {
    private const val TAG = "CentermPos"

    @Volatile
    private var initialized = false

    @Volatile
    var lastInitFailure: String? = null
        private set

    private val pending = mutableListOf<() -> Unit>()
    private val lock = Any()

    fun ensureInitialized(context: Context, onReady: (Boolean) -> Unit) {
        var startInit = false
        synchronized(lock) {
            if (initialized) {
                onReady(true)
                return
            }
            startInit = pending.isEmpty()
            pending += { onReady(initialized) }
        }
        if (!startInit) return

        try {
            Pos.get().init(context.applicationContext, object : InitResultCallback<Void> {
                override fun onStart() {
                    Log.i(TAG, "Pos.init() onStart")
                }

                override fun onResult(result: ActionResult<Void>) {
                    val ok = result.isSuccessful
                    val msg = result.throwable?.message
                    Log.i(
                        TAG,
                        "Pos.init() onResult: code=${result.resultCode}" +
                            " ok=$ok canceled=${result.isCanceled} err=$msg"
                    )
                    result.throwable?.let { Log.e(TAG, "init throwable", it) }
                    val callbacks: List<() -> Unit>
                    synchronized(lock) {
                        if (ok) {
                            initialized = true
                            lastInitFailure = null
                        } else {
                            lastInitFailure = "code=${result.resultCode}" +
                                if (msg != null) " ($msg)" else ""
                        }
                        callbacks = pending.toList()
                        pending.clear()
                    }
                    callbacks.forEach { it() }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Pos.init threw", e)
            val callbacks: List<() -> Unit>
            synchronized(lock) {
                lastInitFailure = "${e.javaClass.simpleName}: ${e.message}"
                callbacks = pending.toList()
                pending.clear()
            }
            callbacks.forEach { it() }
        }
    }
}
