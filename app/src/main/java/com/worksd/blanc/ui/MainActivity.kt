package com.worksd.blanc.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.worksd.blanc.data.BootInfoResponse
import com.worksd.blanc.data.BottomMenuResponse
import com.worksd.blanc.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val TAG = "DORODORO"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initFirebaseMessaging()
        askNotificationPermission()

        binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        val bootInfo = Gson().fromJson(intent.getStringExtra("bootInfo"), BootInfoResponse::class.java)
        lifecycleScope.launch {
            addMainFragment(bootInfo.bottomMenuList)
            if (bootInfo.route != null) {
                val intent = Intent(this@MainActivity, WebViewActivity::class.java)
                intent.putExtra("route", bootInfo.route)
                startActivity(intent)
            }
        }

    }

    private fun initFirebaseMessaging() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d(TAG, "Firebase Token = ${token}")
        })
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
