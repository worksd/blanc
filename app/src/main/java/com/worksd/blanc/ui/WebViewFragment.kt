package com.worksd.blanc.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.worksd.blanc.WebViewListener
import com.worksd.blanc.client.CustomWebViewClient
import com.worksd.blanc.databinding.FragmentWebViewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WebViewFragment : Fragment() {

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

        initWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.apply {
            webView.apply {
                settings.apply {
                    javaScriptEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    allowContentAccess = true
                    domStorageEnabled = true
                    webViewClient = CustomWebViewClient(context, object: WebViewListener {
                        override fun onConnectSuccess() {
                        }

                        override fun onConnectFail() {
                        }

                        override fun onSplashPageStarted() {
                        }
                    })
                }
                loadUrl("http://192.168.0.94:3000/${arguments?.getString("route")}")
            }
        }
    }

    companion object {
        fun newInstance(
            route: String,
        ) = WebViewFragment().apply {
            arguments = Bundle().apply {
                putString("route", route)
            }
        }
    }
}