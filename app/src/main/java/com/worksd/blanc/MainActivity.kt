package com.worksd.blanc

import android.os.Bundle
import android.util.Log
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

        navigate(page = PageInitResponse(route ="/splash", initialColor = "#000000"))

        collectReplace()
        collectPush()
        collectClearAndPush()
        collectBack()
    }

    private fun collectReplace() {
        lifecycleScope.launch {
            viewModel.replace.collect {
                addFragment(
                    page = PageInitResponse(
                        route = it,
                        initialColor = "#FFFFFF"
                    )
                )
            }
        }
    }

    private fun collectPush() {
        lifecycleScope.launch {
            viewModel.push.collect {
                val page = PageInitResponse(
                    route = it,
                    initialColor = "#FFFFFF"
                )
                Log.d("webAppInterface", "good!")
                supportFragmentManager.beginTransaction().apply {
                    val fragment = WebViewFragment.newInstance(
                        url = getUrl(page.route),
                        initialColor = page.initialColor,
                    )
                    add(binding.detailFragmentContainer.id, fragment, page.route)
                    addToBackStack(it)
                    show(fragment)
                    commit()
                }
            }
        }
    }

    private fun collectClearAndPush() {
        lifecycleScope.launch {
            viewModel.clearAndPush.collect {
                if (it == viewModel.mainRoute.value) {
                    addMainFragment(
                        bottomMenuList = viewModel.bottomMenuList.value,
                    )
                } else {
                    launchFragment(
                        PageInitResponse(
                            route = it,
                            initialColor = "#FFFFFF"
                        )
                    )
                }
            }
        }
    }

    private fun collectBack() {
        lifecycleScope.launch {
            viewModel.back.collect {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun addFragment(page: PageInitResponse) {
        supportFragmentManager.beginTransaction().apply {
            val fragment = WebViewFragment.newInstance(
                url = getUrl(page.route),
                initialColor = page.initialColor,
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
            supportFragmentManager.beginTransaction().apply {
                supportFragmentManager.fragments.forEach {
                    remove(it)
                }
                commit()

                supportFragmentManager.fragments.forEach {
                    supportFragmentManager.popBackStack(it.tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }
            }

            bottomMenuList.forEach {
                val fragment = WebViewFragment.newInstance(
                    url = getUrl(it.page.route),
                    initialColor = it.page.initialColor,
                )
                supportFragmentManager.beginTransaction().apply {
                    add(binding.fragmentContainer.id, fragment, it.page.route)
                    hide(fragment)
                    commit()
                }
                delay(100L) // TODO: 하드코딩 수정
                showFragment(bottomMenuList.first().page.route)
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
        return "http://192.168.45.33:3000$route"
    }
}
