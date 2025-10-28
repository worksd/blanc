package com.rawgraphy.blanc.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.rawgraphy.blanc.data.BootInfoResponse
import com.rawgraphy.blanc.data.BottomMenuResponse
import com.rawgraphy.blanc.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()

        binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        try {
            val bootInfo =
                Gson().fromJson(intent.getStringExtra("bootInfo"), BootInfoResponse::class.java)
            Log.d("WebAppInterface", "bootInfo = $bootInfo")
            if (bootInfo != null) {
                lifecycleScope.launch {
                    addMainFragment(bootInfo.bottomMenuList)
                    if (!bootInfo.routeInfo?.route.isNullOrEmpty()) {
                        val intent = Intent(this@MainActivity, WebViewActivity::class.java)
                        intent.putExtra("route", bootInfo.routeInfo.route)
                        intent.putExtra("title", bootInfo.routeInfo.title)
                        intent.putExtra("ignoreSafeArea", bootInfo.routeInfo.ignoreSafeArea)
                        startActivity(intent)
                    }
                }
            }
        } catch (e: Throwable) {
            Log.d("WebAppInterface", "bootInfo = ${e.message}")
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val path = intent.data?.pathSegments?.joinToString("/", prefix = "/").toString()
        navigate(path)
    }

    private fun navigate(route: String) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra("route", route)
        startActivity(intent)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
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
                    route = it.page.route,
                    isBottomMenu = true,
                )
                supportFragmentManager.beginTransaction().apply {
                    add(binding.fragmentContainer.id, fragment, it.page.route)
                    commit()
                }
            }
        }

        binding.bottomNavigation.setContent {

            val currentSelectedRoute = viewModel.currentSelectedBottomRoute.collectAsState().value

            LaunchedEffect(bottomMenuList) {
                if (currentSelectedRoute.isEmpty()) {
                    viewModel.selectBottomMenu(bottomMenuList.firstOrNull()?.page?.route.orEmpty())
                }
            }
            BottomNavigation(
                bottomMenuList = bottomMenuList,
                onClick = { route ->
                    viewModel.selectBottomMenu(route)
                    showFragment(route)
                },
                currentSelectedRoute = currentSelectedRoute,
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
