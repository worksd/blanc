package com.worksd.blanc

interface EventReceiver {
    fun replace(route: String)
    fun push(route: String)
    fun fetchBootInfo(bootInfo: String)
    fun pushAndAllClear(route: String)
    fun back()
    fun setToken(token: String)
}