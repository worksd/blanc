package com.worksd.blanc.client

interface EventReceiver {
    fun replace(route: String)
    fun push(route: String)
    fun showToast(message: String)
    fun navigateMain(bootInfo: String)
    fun pushAndAllClear(route: String)
    fun back()
    fun clearToken()
}