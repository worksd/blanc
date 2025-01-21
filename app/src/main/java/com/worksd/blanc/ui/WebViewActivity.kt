package com.worksd.blanc.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.worksd.blanc.R
import com.worksd.blanc.databinding.ActivityWebViewBinding
import com.worksd.blanc.ui.developer.DeveloperActivity
import dagger.hilt.android.AndroidEntryPoint
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

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
            window.navigationBarColor = android.graphics.Color.parseColor("#000000")
        } else {
            loadWebViewFragment(route)
        }

        getKeyHash()
    }

    private fun getKeyHash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            for (signature in packageInfo.signingInfo.apkContentsSigners) {
                try {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    Log.d("getKeyHash", "key hash: ${Base64.encodeToString(md.digest(), Base64.NO_WRAP)}")
                } catch (e: NoSuchAlgorithmException) {
                    Log.w("getKeyHash", "Unable to get MessageDigest. signature=$signature", e)
                }
            }
        }
    }

    private fun loadWebViewFragment(route: String) {
        supportFragmentManager.beginTransaction().apply {
            val fragment = WebViewFragment.newInstance(
                route = route,
            )
            replace(binding.fragmentContainer.id, fragment, route)
            show(fragment)
            commit()
        }
    }

    private fun loadSplashScreen() {
        binding.splashScreen.setContent {
            SplashScreen(
                startDevelopMode = {
                    val intent = Intent(this, DeveloperActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@Composable
private fun SplashScreen(
    startDevelopMode: () -> Unit,
) {
    val clickCount = remember { mutableStateOf(0) }
    val lastClickTime = remember { mutableStateOf(0L) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastClickTime.value < 1000) {
                        clickCount.value++
                        if (clickCount.value >= 5) {
                            startDevelopMode()
                            clickCount.value = 0
                        }
                    } else {
                        clickCount.value = 1
                    }

                    lastClickTime.value = currentTime
                }
            },
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Logo",
        )
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    SplashScreen({})
}