package com.worksd.blanc.ui

import android.os.Bundle
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
import com.worksd.blanc.R
import com.worksd.blanc.databinding.ActivityWebViewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val route = intent.getStringExtra("route")

        if (route.isNullOrEmpty()) {
            loadSplashScreen()
            loadWebViewFragment("/splash")
        } else {
            loadWebViewFragment(route)
        }
    }

    private fun loadWebViewFragment(route: String) {
        supportFragmentManager.beginTransaction().apply {
            val fragment = WebViewFragment.newInstance(
                route = route,
                initialColor = "#FFFFFF",
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
        modifier = Modifier.fillMaxSize().background(Color.Black),
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