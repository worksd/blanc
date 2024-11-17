package com.worksd.blanc

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.worksd.blanc.client.CustomWebViewClient
import com.worksd.blanc.data.BottomMenuResponse
import com.worksd.blanc.databinding.ActivityMainBinding
import com.worksd.blanc.ui.BottomNavigation
import com.worksd.blanc.ui.MainViewModel
import com.worksd.blanc.ui.WebViewFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BlancActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        navigate("splash")
    }

    private fun addFragment(route: String) {
        supportFragmentManager.beginTransaction().apply {
            val fragment = WebViewFragment.newInstance(route)
            replace(binding.detailFragmentContainer.id, fragment, route)
            show(fragment)
            commit()
        }
    }

    private fun addMainFragment(
        bottomMenuList: List<BottomMenuResponse>
    ) {
        lifecycleScope.launch {
            bottomMenuList.forEach {
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
                }
            )
        }
    }

    private fun showFragment(route: String) {
        supportFragmentManager.beginTransaction().apply {
            supportFragmentManager.fragments.forEach { hide(it) }
            supportFragmentManager.findFragmentByTag(route)?.let { show(it) }
            commit()
        }
    }

    private fun navigate(route: String, withClear: Boolean = false) {
        if (withClear) { supportFragmentManager.fragments.forEach {
            supportFragmentManager.beginTransaction().remove(it).commit()
            supportFragmentManager.popBackStack(it.tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }
        if (route == "main") {
            addMainFragment(bottomMenuList)
        }
        else {
            addFragment(route)
        }

        lifecycleScope.launch {
            if (route == "splash") {
                delay(2000L)
                navigate("login", true)
                delay(2000L)
                navigate("main", true)
            }
        }
    }

    private val bottomMenuList = listOf(
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
}
