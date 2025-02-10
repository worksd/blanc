package com.rawgraphy.blanc.client

interface WebViewListener {
    fun onConnectFail()
    fun onConnectSuccess()
    fun onPageFinished()
}