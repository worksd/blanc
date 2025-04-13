package com.rawgraphy.blanc.util

import android.content.Context

object KloudWebUrlProvider {
    private const val DEFAULT_PRODUCTION_URL = "https://rawgraphy.com"
    fun getUrl(context: Context, route: String): String {
        return "${PrefUtils(context).getString(WebEndPointKey) ?: DEFAULT_PRODUCTION_URL}$route"
    }
}