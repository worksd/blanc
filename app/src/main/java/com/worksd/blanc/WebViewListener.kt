package com.worksd.blanc

import android.webkit.WebResourceError

interface WebViewListener {

    fun onSplashPageStarted()
    fun onConnectFail()
    fun onConnectSuccess()
}