package com.rawgraphy.blanc.util

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.UUID

val refreshWebView: MutableSharedFlow<List<String>> = MutableSharedFlow()

fun getInstallationId(context: Context): String {
    val pref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val key = "installation_id"
    var id = pref.getString(key, null)
    if (id == null) {
        id = UUID.randomUUID().toString()
        pref.edit().putString(key, id).apply()
    }
    return id
}