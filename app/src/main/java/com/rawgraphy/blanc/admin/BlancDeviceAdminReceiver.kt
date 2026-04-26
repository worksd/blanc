package com.rawgraphy.blanc.admin

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Device Admin / Device Owner 리시버.
 *
 * ADB로 Device Owner 지정:
 *   adb shell dpm set-device-owner com.rawgraphy.blanc/.admin.BlancDeviceAdminReceiver
 *
 * 초기화된 기기(계정 미등록 상태)에서만 지정 가능.
 * 지정되면 이 리시버가 루트 관리자이며 키오스크 정책을 집행.
 */
class BlancDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "Device admin enabled")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.i(TAG, "Device admin disabled")
    }

    companion object {
        private const val TAG = "BlancDeviceAdmin"

        fun componentName(context: Context): ComponentName =
            ComponentName(context, BlancDeviceAdminReceiver::class.java)
    }
}
