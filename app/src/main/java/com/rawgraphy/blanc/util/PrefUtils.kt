package com.rawgraphy.blanc.util

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

const val FILE_NAME = "Rawgraphy"
const val WebEndPointKey = "WebEndPointKey"

class PrefUtils @Inject constructor(
    val context: Context
) {

    private fun getSharedPreferences(
        fileName: String?,
        mode: Int = Context.MODE_PRIVATE
    ): SharedPreferences {
        return context.getSharedPreferences(fileName, mode)
    }

    fun setString(key: String?, value: String?): Boolean {
        val pref = getSharedPreferences(FILE_NAME)
        val editor = pref.edit()
        editor.putString(key, value)
        return editor.commit()
    }

    fun setInt(fileName: String?, key: String?, value: Int): Boolean {
        val pref = getSharedPreferences(fileName)
        val editor = pref.edit()
        editor.putInt(key, value)
        return editor.commit()
    }

    fun getString(key: String?): String? {
        val pref = getSharedPreferences(FILE_NAME)
        return pref.getString(key, null)
    }

    fun getLong(fileName: String?, key: String): Long {
        val pref = getSharedPreferences(fileName)
        return pref.getLong(key, 0L)
    }

    fun setLong(fileName: String?, key: String?, value: String?): Boolean {
        val pref = getSharedPreferences(fileName)
        val editor = pref.edit()
        editor.putLong(key, value!!.toLong())
        return editor.commit()
    }

    fun getInt(fileName: String?, key: String): Int {
        val pref = getSharedPreferences(fileName)
        return pref.getInt(key, 0)
    }

    fun remove(fileName: String?, key: String?): Boolean {
        val pref = getSharedPreferences(fileName)
        return pref.edit().remove(key).commit()
    }
}