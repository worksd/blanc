package com.worksd.blanc.util

object KloudWebUrlProvider {
    fun getUrl(route: String): String {
        return "http://192.168.45.244:3000$route"
    }
}