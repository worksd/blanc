package com.worksd.blanc.ui

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.worksd.blanc.EventReceiver
import com.worksd.blanc.client.CustomWebViewClient

@Composable
fun SplashScreen(
    listener: EventReceiver,
    client: CustomWebViewClient,
    onLoadingStart: () -> Unit,
) {

    LaunchedEffect(Unit) {
        onLoadingStart()
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        WebScreen(
            route = "splash",
            webInterface = listener,
            client = client,
        )
    }
}