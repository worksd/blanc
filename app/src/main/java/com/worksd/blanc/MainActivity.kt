package com.worksd.blanc

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.worksd.blanc.data.BottomMenuResponse
import com.worksd.blanc.data.PageInitResponse
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

        navigate(page = PageInitResponse(route ="splash", initialColor = "#000000"))
    }

    private fun addFragment(page: PageInitResponse) {
        supportFragmentManager.beginTransaction().apply {
            val fragment = WebViewFragment.newInstance(
                url = getUrl(page.route),
                initialColor = page.initialColor,
            )
            setCustomAnimations(
                R.anim.anim_slide_in,
                R.anim.anim_fade_out,
            )
            replace(binding.detailFragmentContainer.id, fragment, page.route)
            show(fragment)
            commit()
        }
    }

    private fun addMainFragment(
        bottomMenuList: List<BottomMenuResponse>
    ) {
        lifecycleScope.launch {
            bottomMenuList.forEach {
                val fragment = WebViewFragment.newInstance(
                    url = getUrl(it.page.route),
                    initialColor = it.page.initialColor,
                )
                supportFragmentManager
                    .beginTransaction()
                    .add(binding.fragmentContainer.id, fragment, it.page.route)
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
        val transaction = supportFragmentManager.beginTransaction()
            supportFragmentManager.fragments.forEach { transaction.hide(it) }
            supportFragmentManager.findFragmentByTag(route)?.let { transaction.show(it) }
            transaction.commit()
    }

    private fun launchFragment(page: PageInitResponse) {
        supportFragmentManager.fragments.forEach {
            supportFragmentManager.beginTransaction().remove(it).commit()
            supportFragmentManager.popBackStack(
                it.tag,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
        addFragment(page)
    }

    private fun navigate(page: PageInitResponse) {

        addFragment(
            page = page,
        )


        lifecycleScope.launch {
            if (page.route == "splash") {
                delay(2000L)
                launchFragment(page = PageInitResponse(route ="login", initialColor = "#000000"))
                delay(2000L)
                addMainFragment(bottomMenuList)
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
            page = PageInitResponse(route ="home", initialColor = "#000000"),
        ),
        BottomMenuResponse(
            label = "프로필",
            labelSize = 12,
            labelColor = "#000000",
            iconUrl = "",
            iconSize = 24,
            page = PageInitResponse(route ="profile", initialColor = "#000000"),
        ),
        BottomMenuResponse(
            label = "알림",
            labelSize = 12,
            labelColor = "#000000",
            iconUrl = "",
            iconSize = 24,
            page = PageInitResponse(route ="notifications", initialColor = "#000000"),
        )
    )

    private fun getUrl(route: String): String {
        return "http://192.168.0.94:3000/$route"
    }
}
