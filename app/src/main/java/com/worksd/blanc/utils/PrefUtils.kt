package com.worksd.blanc.utils

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class PrefUtils @Inject constructor(
    val context: Context
) {

    private fun getSharedPreferences(
        fileName: String?,
        mode: Int = Context.MODE_PRIVATE
    ): SharedPreferences {
        return context.getSharedPreferences(fileName, mode)
    }

    fun setString(fileName: String?, key: String?, value: String?): Boolean {
        val pref = getSharedPreferences(fileName)
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

    fun getString(fileName: String?, key: String?): String? {
        val pref = getSharedPreferences(fileName)
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