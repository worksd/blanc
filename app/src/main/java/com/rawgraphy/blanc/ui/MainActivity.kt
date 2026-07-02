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
import com.rawgraphy.blanc.data.RouteInfo
import com.rawgraphy.blanc.databinding.ActivityMainBinding
import com.rawgraphy.blanc.util.applyKioskImmersiveIfNeeded
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

        applyKioskImmersiveIfNeeded()

        // 딥링크 콜드 스타트 (iOS onCreate). 딥링크로 진입한 경우 bootInfo 처리 없이 종료.
        if (handleDeepLink(intent)) return

        try {
            val bootInfo =
                Gson().fromJson(intent.getStringExtra("bootInfo"), BootInfoResponse::class.java)
            if (bootInfo != null) {
                lifecycleScope.launch {
                    addMainFragment(bootInfo.bottomMenuList)
                    try {
                        val routeInfo = Gson().fromJson(bootInfo.route, RouteInfo::class.java)
                        if (routeInfo.route?.isEmpty() == false) {
                            val intent = Intent(this@MainActivity, WebViewActivity::class.java)
                            intent.putExtra("route", bootInfo.route)
                            startActivity(intent)
                        }
                    } catch (e: Throwable) {

                    }
                }
            }
        } catch (e: Throwable) {
            Log.d("WebAppInterface", "bootInfo = ${e.message}")
        }
    }

    override fun onNewIntent(intent: Intent) {   // 앱이 떠 있을 때 (iOS 웜 케이스)
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    /**
     * iOS handleDeepLink와 동일 로직.
     * https / rawgraphy:// 모두에서 원본 path(+query)를 복원해
     * /splash?link=<encoded> 를 WebViewActivity route로 전달한다.
     * @return 딥링크를 처리했으면 true.
     */
    private fun handleDeepLink(intent: Intent?): Boolean {
        val uri = intent?.data ?: return false
        Log.d("deeplink", "incoming uri=$uri")

        // https://.../redirect → /redirect,  rawgraphy://redirect → /redirect
        val path = if (uri.scheme == "https" || uri.scheme == "http") {
            uri.encodedPath ?: ""                                  // iOS url.path
        } else {
            "/" + (uri.host ?: "") + (uri.encodedPath ?: "")       // 커스텀 스킴: host가 첫 세그먼트
        }
        // encodedQuery(원본 미디코드) 사용 → iOS url.query와 패리티
        val full = if (!uri.encodedQuery.isNullOrEmpty()) "$path?${uri.encodedQuery}" else path

        if (full.length <= 1) {
            navigate("/splash")
            return true
        }

        val encodedLink = percentEncodeAlnumOnly(full)             // iOS .alphanumerics와 동일
        val target = "/splash?link=$encodedLink"
        Log.d("deeplink", "navigate $target")
        navigate(target)
        return true
    }

    // iOS: path.addingPercentEncoding(withAllowedCharacters: .alphanumerics)
    // → 영숫자 외 전부 %XX (UTF-8 바이트 단위). Uri.encode()는 허용 문자가 달라 쓰지 않는다.
    private fun percentEncodeAlnumOnly(s: String): String =
        buildString {
            for (b in s.toByteArray(Charsets.UTF_8)) {
                val c = b.toInt() and 0xFF
                val ch = c.toChar()
                if (ch in 'A'..'Z' || ch in 'a'..'z' || ch in '0'..'9') {
                    append(ch)
                } else {
                    append("%%%02X".format(c))
                }
            }
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
                val routeJson = Gson().toJson(
                    RouteInfo(
                        route = it.page.route,
                        ignoreSafeArea = it.page.ignoreSafeArea,
                        title = null,
                    )
                )
                val fragment = WebViewFragment.newInstance(
                    route = routeJson,
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
