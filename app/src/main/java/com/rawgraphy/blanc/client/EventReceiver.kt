package com.rawgraphy.blanc.client

import com.rawgraphy.blanc.data.GoogleLoginConfiguration
import com.rawgraphy.blanc.data.KloudDialogInfo

interface EventReceiver {
    fun replace(route: String)
    fun push(route: String)
    fun showToast(message: String)
    fun fullSheet(route: String)
    fun navigateMain(bootInfo: String)
    fun pushAndAllClear(route: String)
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
}