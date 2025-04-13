package com.rawgraphy.blanc.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rawgraphy.blanc.R
import com.rawgraphy.blanc.ui.WebViewActivity
import com.rawgraphy.blanc.util.refreshWebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FirebaseMessaging : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)


    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("FirebaseMessaging", "New Token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: message.data["title"].orEmpty()
        val body = message.notification?.body ?: message.data["body"].orEmpty()
        val route = message.data["route"].orEmpty()
        val hideNotification = message.data["hideNotification"].orEmpty()
        val endpointsToRefresh = message.data["endpointsToRefresh"].orEmpty().split(',')

        if (endpointsToRefresh.isNotEmpty()) {
            serviceScope.launch {
                refreshWebView.emit(endpointsToRefresh)
            }
        }

        Log.d("FirebaseMessaging", "title = $title, body = $body route = $route, hideNotification = $hideNotification")
        if (hideNotification == "true") return
        // Intent 생성
        val intent = Intent(this, WebViewActivity::class.java).apply {
            putExtra("route", route)
        }

        // PendingIntent 생성
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification 생성 및 표시
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "default"

        // Android Oreo 이상에서는 채널 생성 필요
        val channel = NotificationChannel(
            channelId,
            "Default Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_logo) // 알림 아이콘 필요
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}