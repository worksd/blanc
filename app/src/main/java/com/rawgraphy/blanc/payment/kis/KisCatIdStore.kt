package com.rawgraphy.blanc.payment.kis

import android.content.Context
import android.util.Log

/**
 * KIS-ANDAGT 단말의 catId(TID) 캐시.
 * 단말 catId는 한 번 결정되면 평생 안 바뀌므로, KIS 응답에서 outCatId를 본 시점에 한 번
 * SharedPreferences에 박아두고, 이후 ST 조회 등에 자동으로 사용한다.
 */
object KisCatIdStore {
    private const val TAG = "KisCatIdStore"
    private const val PREF_NAME = "Rawgraphy"
    private const val KEY = "kis_cat_id"

    fun save(context: Context, catId: String) {
        if (catId.isBlank()) return
        val prev = get(context)
        if (prev == catId) {
            Log.i(TAG, "save() noop — already cached catId=$catId")
            return
        }
        // commit() 으로 동기 flush — 크래시 직후에도 디스크에 남도록 보장
        val ok = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, catId)
            .commit()
        Log.i(TAG, "save catId=$catId (was=$prev) committed=$ok")
    }

    fun get(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY, null)
    }
}
