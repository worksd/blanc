package com.worksd.blanc.client

import com.worksd.blanc.data.KloudDialogInfo

interface EventReceiver {
    fun replace(route: String)
    fun push(route: String)
    fun showToast(message: String)
    fun navigateMain(bootInfo: String)
    fun pushAndAllClear(route: String)
    fun back()
    fun clearToken()
    fun sendHapticFeedback()
    fun sendKakaoLogin()
    fun sendGoogleLogin()
    fun showDialog(dialogInfo: KloudDialogInfo)
    fun showBottomSheet(bottomSheetInfo: String)
}