package com.rawgraphy.blanc.util

import android.content.Context

object KloudWebUrlProvider {
    private const val DEFAULT_PRODUCTION_URL = "https://kloud-git-develop-rawgraphy-inc.vercel.app"
    fun getUrl(context: Context, route: String): String {
        return "${PrefUtils(context).getString(WebEndPointKey) ?: DEFAULT_PRODUCTION_URL}$route"
    }
}