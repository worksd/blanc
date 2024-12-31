package com.worksd.blanc

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.worksd.blanc.data.BottomMenuResponse
import com.worksd.blanc.data.PageInitResponse
import com.worksd.blanc.databinding.ActivityMainBinding
import com.worksd.blanc.ui.BottomNavigation
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

        navigate(page = PageInitResponse(route = "/splash", initialColor = "#000000"))

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
                    initialColor = "#FFFFFFF"
                )
                Log.d("webAppInterface", "good!")
                supportFragmentManager.beginTransaction().apply {
                    val fragment = WebViewFragment.newInstance(
                        url = getUrl(page.route),
                        initialColor = "#FFFFFF",
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
                            initialColor = "#FFFFFFF"
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
                initialColor = "#FFFFFF",
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
                    supportFragmentManager.popBackStack(
                        it.tag,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                }
            }

            bottomMenuList.reversed().forEach {
                val fragment = WebViewFragment.newInstance(
                    url = getUrl(it.page.route),
                    initialColor = it.page.initialColor,
                )
                supportFragmentManager.beginTransaction().apply {
                    add(binding.fragmentContainer.id, fragment, it.page.route)
                    commit()
                }

            }
            viewModel.selectBottomMenu(bottomMenuList.first().page.route)
        }

        binding.bottomNavigation.setContent {
            BottomNavigation(
                bottomMenuList = bottomMenuList,
                onClick = { route ->
                    viewModel.selectBottomMenu(route)
                    showFragment(route)
                },
                currentSelectedRoute = viewModel.currentSelectedIndex.collectAsState().value
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

    private fun getUrl(route: String): String {
        return "https://kloud-alpha.vercel.app/$route"
    }
}
