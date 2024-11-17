package com.worksd.blanc.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.worksd.blanc.WebViewListener
import com.worksd.blanc.client.CustomWebViewClient
import com.worksd.blanc.databinding.FragmentWebViewBinding

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
                root.setBackgroundColor(Color.parseColor(initialColor))
                webView.apply {
                    settings.apply {
                        javaScriptEnabled = true
                        setSupportZoom(false)
                        builtInZoomControls = false
                        displayZoomControls = false
                        allowContentAccess = true
                        domStorageEnabled = true
                        setBackgroundColor(Color.parseColor(initialColor))
                        webViewClient = CustomWebViewClient(context, object : WebViewListener {
                            override fun onConnectSuccess() {
                            }

                            override fun onConnectFail() {
                            }

                            override fun onSplashPageStarted() {
                            }
                        })
                    }
                    loadUrl(url)
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