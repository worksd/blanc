package com.rawgraphy.blanc.util

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.rawgraphy.blanc.admin.BlancDeviceAdminReceiver

/**
 * Android Lock Task(화면 고정) 기반 키오스크 모드.
 *
 * - 일반 앱: `startLockTask` 호출 시 Android가 확인 다이얼로그 띄움 → 사용자가 뒤로+최근 길게 눌러 탈출 가능.
 * - Device Owner로 지정된 경우: 확인 없이 바로 고정되며 사용자 탈출 불가.
 *
 * 키오스크 진입 시 화면 항상 켜짐 + 시스템 바(상단/하단) 숨김까지 같이 적용.
 *
 * Device Owner는 초기화된 기기에서 ADB로 한 번 지정해야 함:
 *   adb shell dpm set-device-owner com.rawgraphy.blanc/.admin.BlancDeviceAdminReceiver
 */
data class KioskState(
    val action: String,                  // "enter" | "exit"
    val success: Boolean,
    val isDeviceOwner: Boolean,
    val deviceOwnerPolicyApplied: Boolean,
    val lockTaskActive: Boolean,
    val lockTaskModeState: Int,          // 0=NONE, 1=LOCKED, 2=PINNED
    val lockTaskModeName: String,
    val sdkInt: Int = Build.VERSION.SDK_INT,
    val error: String? = null,
)

object KioskMode {
    private const val TAG = "KioskMode"

    fun enter(activity: Activity, onResult: (KioskState) -> Unit = {}) {
        activity.runOnUiThread {
            Log.i(TAG, "▶ enter() requested  pkg=${activity.packageName}")
            val before = readLockTaskState(activity)
            Log.i(TAG, "  state before: $before (${stateName(before)})")

            val isDO = isDeviceOwner(activity)
            Log.i(TAG, "  isDeviceOwner=$isDO")

            val doApplied = if (isDO) applyDeviceOwnerPolicy(activity) else false

            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            applyImmersive(activity, hide = true)
            Log.i(TAG, "  immersive=on, keepScreenOn=on")

            var error: String? = null
            if (before == ActivityManager.LOCK_TASK_MODE_NONE) {
                try {
                    activity.startLockTask()
                    Log.i(TAG, "  startLockTask() called")
                } catch (e: Exception) {
                    error = e.message ?: e.javaClass.simpleName
                    Log.e(TAG, "  startLockTask FAILED", e)
                }
            } else {
                Log.i(TAG, "  startLockTask SKIPPED (already in lock task)")
            }

            val after = readLockTaskState(activity)
            val state = KioskState(
                action = "enter",
                success = error == null && after != ActivityManager.LOCK_TASK_MODE_NONE,
                isDeviceOwner = isDO,
                deviceOwnerPolicyApplied = doApplied,
                lockTaskActive = after != ActivityManager.LOCK_TASK_MODE_NONE,
                lockTaskModeState = after,
                lockTaskModeName = stateName(after),
                error = error,
            )
            Log.i(TAG, "◀ enter() result: $state")
            onResult(state)
        }
    }

    fun exit(activity: Activity, onResult: (KioskState) -> Unit = {}) {
        activity.runOnUiThread {
            Log.i(TAG, "▶ exit() requested  pkg=${activity.packageName}")
            val before = readLockTaskState(activity)
            Log.i(TAG, "  state before: $before (${stateName(before)})")

            var error: String? = null
            if (before != ActivityManager.LOCK_TASK_MODE_NONE) {
                try {
                    activity.stopLockTask()
                    Log.i(TAG, "  stopLockTask() called")
                } catch (e: Exception) {
                    error = e.message ?: e.javaClass.simpleName
                    Log.e(TAG, "  stopLockTask FAILED", e)
                }
            } else {
                Log.i(TAG, "  stopLockTask SKIPPED (not in lock task)")
            }

            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            applyImmersive(activity, hide = false)
            Log.i(TAG, "  immersive=off, keepScreenOn=off")

            val after = readLockTaskState(activity)
            val state = KioskState(
                action = "exit",
                success = error == null && after == ActivityManager.LOCK_TASK_MODE_NONE,
                isDeviceOwner = isDeviceOwner(activity),
                deviceOwnerPolicyApplied = false,
                lockTaskActive = after != ActivityManager.LOCK_TASK_MODE_NONE,
                lockTaskModeState = after,
                lockTaskModeName = stateName(after),
                error = error,
            )
            Log.i(TAG, "◀ exit() result: $state")
            onResult(state)
        }
    }

    fun isActive(activity: Activity): Boolean =
        readLockTaskState(activity) != ActivityManager.LOCK_TASK_MODE_NONE

    private fun readLockTaskState(activity: Activity): Int {
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.lockTaskModeState
    }

    private fun stateName(state: Int): String = when (state) {
        ActivityManager.LOCK_TASK_MODE_NONE -> "NONE"
        ActivityManager.LOCK_TASK_MODE_LOCKED -> "LOCKED"
        ActivityManager.LOCK_TASK_MODE_PINNED -> "PINNED"
        else -> "UNKNOWN($state)"
    }

    private fun isDeviceOwner(activity: Activity): Boolean {
        val dpm = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        return dpm.isDeviceOwnerApp(activity.packageName)
    }

    private fun applyDeviceOwnerPolicy(activity: Activity): Boolean {
        val dpm = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        val component = BlancDeviceAdminReceiver.componentName(activity)
        return try {
            dpm.setLockTaskPackages(component, arrayOf(activity.packageName))
            Log.i(TAG, "  setLockTaskPackages([${activity.packageName}]) ok")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                dpm.setLockTaskFeatures(
                    component,
                    DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO or
                        DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS
                )
                Log.i(TAG, "  setLockTaskFeatures(SYSTEM_INFO|GLOBAL_ACTIONS) ok")
            }
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "  Device owner policy rejected", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "  Device owner policy config failed", e)
            false
        }
    }

    private fun applyImmersive(activity: Activity, hide: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, !hide)
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        if (hide) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
