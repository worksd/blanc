package com.worksd.blanc.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.worksd.blanc.EventReceiver
import com.worksd.blanc.WebViewListener
import com.worksd.blanc.client.CustomWebViewClient
import com.worksd.blanc.client.WebAppInterface
import com.worksd.blanc.databinding.FragmentWebViewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.worksd.blanc.utils.PrefKeys.Session.FILE_NAME
import com.worksd.blanc.utils.PrefKeys.Session.KEY_TOKEN
import com.worksd.blanc.utils.PrefUtils

@AndroidEntryPoint
class WebViewFragment : Fragment() {

    private val cookieManager by lazy { CookieManager.getInstance()}
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var binding: FragmentWebViewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initWebView(
            url = requireArguments().getString(ARG_URL).orEmpty(),
            initialColor = requireArguments().getString(ARG_INITIAL_COLOR).orEmpty()
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(
        url: String, initialColor: String
    ) {
        try {

            binding.apply {
                cookieManager.apply {
                    this.acceptCookie()
                    this.acceptThirdPartyCookies(webView)
                }
                root.setBackgroundColor(Color.parseColor(initialColor))
                webView.apply {
                    settings.apply {
                        javaScriptEnabled = true
                        setSupportZoom(false)
                        builtInZoomControls = false
                        displayZoomControls = false
                        allowContentAccess = true
                        domStorageEnabled = true
                        val customWebViewClient =
                            CustomWebViewClient(context, object : WebViewListener {
                                override fun onConnectSuccess() {

                                }

                                override fun onConnectFail() {
                                }

                                override fun onSplashPageStarted() {
                                }
                            })
                        addJavascriptInterface(WebAppInterface(object :EventReceiver {
                            override fun fetchBootInfo(bootInfo: String) {
                                viewModel.setBootInfo(bootInfo)
                            }

                            override fun replace(route: String) {
                                Log.d("WebAppInterface", "replace = $route")
                                viewModel.navigate(route)
                            }

                            override fun push(route: String) {
                                Log.d("WebAppInterface", "push = $route")
                                viewModel.push(route)
                            }

                            override fun pushAndAllClear(route: String) {
                                Log.d("WebAppInterface", "clear And Push = $route")
                                viewModel.clearAndPush(route)
                            }

                            override fun back() {
                                viewModel.back()
                            }

                            override fun setToken(token: String) {
                                Log.d("WebAppInterface", "setToken = $token")
                            }

                            override fun clearToken() {
                                cookieManager.removeAllCookies(null)
                            }
                        }), "KloudEvent")
                        setBackgroundColor(Color.parseColor(initialColor))
                        webViewClient = customWebViewClient
                        Log.d("WebAppInterface", "initWebView: $")
                        Log.d("WebAppInterface", "cookie: ${cookieManager.getCookie(url)}")
                        loadUrl(url)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("WebAppInterface", "initWebView: $e")
        }
    }

    companion object {
        private const val ARG_URL = "ARG_URL"
        private const val ARG_INITIAL_COLOR = "ARG_INITIAL_COLOR"
        fun newInstance(
            url: String,
            initialColor: String,
        ) = WebViewFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_URL, url)
                putString(ARG_INITIAL_COLOR, initialColor)
            }
        }
    }
}