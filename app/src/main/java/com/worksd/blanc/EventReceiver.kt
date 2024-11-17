package com.worksd.blanc

interface EventReceiver {
    fun navigate(route: String)
    fun fetchBootInfo(bootInfo: String)
    fun pushAndAllClear(route: String)
}