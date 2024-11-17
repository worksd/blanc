package com.worksd.blanc

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEachIndexed
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.worksd.blanc.data.BootInfoResponse
import com.worksd.blanc.data.BottomMenuResponse
import com.worksd.blanc.databinding.ActivityMainBinding
import com.worksd.blanc.ui.BlancScreen
import com.worksd.blanc.ui.BottomNavigation
import com.worksd.blanc.ui.MainScreen
import com.worksd.blanc.ui.MainViewModel
import com.worksd.blanc.ui.WebViewFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BlancActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        val bottomMenuList = listOf(
            BottomMenuResponse(
                label = "홈",
                labelSize = 12,
                labelColor = "#000000",
                iconUrl = "",
                iconSize = 24,
                url = "home"
            ),
            BottomMenuResponse(
                label = "프로필",
                labelSize = 12,
                labelColor = "#000000",
                iconUrl = "",
                iconSize = 24,
                url = "profile"
            ),
            BottomMenuResponse(
                label = "알림",
                labelSize = 12,
                labelColor = "#000000",
                iconUrl = "",
                iconSize = 24,
                url = "notifications"
            )
        )

        lifecycleScope.launch {
            bottomMenuList.reversed().forEach {
                val fragment = WebViewFragment.newInstance(route = it.url)
                supportFragmentManager
                    .beginTransaction()
                    .add(binding.fragmentContainer.id, fragment, it.url)
                    .commit()
            }
        }



        binding.bottomNavigation.setContent {
            BottomNavigation(
                bottomMenuList = bottomMenuList,
                onClick = { route ->
                    showFragment(route)
                    if (route == "notifications") {
                        addSplashFragment()
                    }
                }
            )
        }

    }

    private fun addSplashFragment() {
        supportFragmentManager.beginTransaction().apply {
            val fragment = WebViewFragment.newInstance("splash")
            add(binding.detailFragmentContainer.id, fragment, "splash")
            addToBackStack(null)
            show(fragment)
            commit()
        }
    }

    private fun showFragment(route: String) {
        supportFragmentManager.beginTransaction().apply {
            supportFragmentManager.fragments.forEach { hide(it) }
            supportFragmentManager.findFragmentByTag(route)?.let { show(it) }
            commit()
        }
    }

}
