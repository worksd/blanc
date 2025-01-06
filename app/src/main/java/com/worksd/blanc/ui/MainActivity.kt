package com.worksd.blanc.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.worksd.blanc.data.BootInfoResponse
import com.worksd.blanc.data.BottomMenuResponse
import com.worksd.blanc.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        val bootInfo = Gson().fromJson(intent.getStringExtra("bootInfo"), BootInfoResponse::class.java)
        addMainFragment(bootInfo.bottomMenuList)
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
                    route = it.page.route,
                    initialColor = it.page.initialColor,
                )
                supportFragmentManager.beginTransaction().apply {
                    add(binding.fragmentContainer.id, fragment, it.page.route)
                    commit()
                }
            }
        }

        binding.bottomNavigation.setContent {
            val selectedRoute = remember { mutableStateOf(bottomMenuList.first().page.route)}
            BottomNavigation(
                bottomMenuList = bottomMenuList,
                onClick = { route ->
                    selectedRoute.value = route
                    showFragment(route)
                },
                currentSelectedRoute = selectedRoute.value
            )
        }
    }

    private fun showFragment(route: String) {
        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach { transaction.hide(it) }
        supportFragmentManager.findFragmentByTag(route)?.let { transaction.show(it) }
        transaction.commit()
    }
}
