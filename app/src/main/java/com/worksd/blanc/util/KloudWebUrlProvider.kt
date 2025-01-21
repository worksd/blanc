package com.worksd.blanc.util

import android.content.Context

object KloudWebUrlProvider {
    fun getUrl(context: Context, route: String): String {
        return "${PrefUtils(context).getString(WebEndPointKey).orEmpty()}$route"
    }
}