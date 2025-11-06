package com.rawgraphy.blanc.ui

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.rawgraphy.blanc.R
import com.rawgraphy.blanc.databinding.ActivityWebViewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initWebView()
    }

    private fun initWebView() {
        val route = intent.getStringExtra("route")
        val title = intent.getStringExtra("title")
        val ignoreSafeArea = intent.getBooleanExtra("ignoreSafeArea", false)
        if (route.isNullOrEmpty()) {
            loadSplashScreen()
            loadWebViewFragment("/splash", null, true)
            window.navigationBarColor = android.graphics.Color.parseColor("#000000")
        } else {
            loadWebViewFragment(route, title, ignoreSafeArea)
        }
    }

    private fun loadWebViewFragment(route: String, title: String?, ignoreSafeArea: Boolean) {
        supportFragmentManager.beginTransaction().apply {
            val fragment = WebViewFragment.newInstance(
                route = route,
                title = title,
                ignoreSafeArea = ignoreSafeArea,
                isBottomMenu = false,
            )
            replace(binding.fragmentContainer.id, fragment, route)
            show(fragment)
            commit()
        }
    }

    private fun loadSplashScreen() {
        binding.splashScreen.setContent {
            SplashScreen()
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Logo",
        )
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    SplashScreen()
}