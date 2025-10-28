package com.rawgraphy.blanc.client

import com.rawgraphy.blanc.data.GoogleLoginConfiguration
import com.rawgraphy.blanc.data.KloudDialogInfo
import com.rawgraphy.blanc.data.RouteInfo

interface EventReceiver {
    fun replace(route: String)
    fun push(routeInfo: RouteInfo)
    fun showToast(message: String)
    fun fullSheet(routeInfo: RouteInfo)
    fun navigateMain(bootInfo: String)
    fun pushAndAllClear(routeInfo: RouteInfo)
    fun back()
    fun clearToken()
    fun sendHapticFeedback()
    fun sendKakaoLogin()
    fun sendGoogleLogin(configuration: GoogleLoginConfiguration)
    fun showDialog(dialogInfo: KloudDialogInfo)
    fun requestPayment(command: String)
    fun sendFcmToken()
    fun showBottomSheet(route: String)
    fun closeBottomSheet()
    fun changeWebEndpoint(endpoint: String)
    fun openExternalBrowser(url: String)
    fun refresh(endpoint: String)
}