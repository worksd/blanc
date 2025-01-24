package com.worksd.blanc.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.worksd.blanc.R
import com.worksd.blanc.client.CustomWebViewClient
import com.worksd.blanc.client.EventReceiver
import com.worksd.blanc.client.WebAppInterface
import com.worksd.blanc.client.WebViewListener
import com.worksd.blanc.client.onDialogConfirm
import com.worksd.blanc.client.onErrorInvoked
import com.worksd.blanc.client.onGoogleLoginSuccess
import com.worksd.blanc.client.onHideDialog
import com.worksd.blanc.client.onKakaoLoginSuccess
import com.worksd.blanc.client.onPaymentSuccess
import com.worksd.blanc.data.GoogleLoginConfiguration
import com.worksd.blanc.data.KloudDialogInfo
import com.worksd.blanc.data.PaymentInfo
import com.worksd.blanc.databinding.FragmentWebViewBinding
import com.worksd.blanc.util.KloudWebUrlProvider
import dagger.hilt.android.AndroidEntryPoint
import io.portone.sdk.android.PortOne
import io.portone.sdk.android.payment.PaymentCallback
import io.portone.sdk.android.payment.PaymentRequest
import io.portone.sdk.android.payment.PaymentResponse
import io.portone.sdk.android.type.Amount
import io.portone.sdk.android.type.Currency
import io.portone.sdk.android.type.Customer
import io.portone.sdk.android.type.PaymentMethod
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WebViewFragment : Fragment() {

    private val cookieManager by lazy { CookieManager.getInstance() }
    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: FragmentWebViewBinding
    private var isLoading: Boolean = true
    private var toast: Toast? = null

    private val paymentActivityResultLauncher =
        PortOne.registerForPaymentActivity(this, callback = object :
            PaymentCallback {
            override fun onSuccess(response: PaymentResponse.Success) {
                Log.d("NativeLog", "onSuccess: $response")
                binding.webView.onPaymentSuccess(
                    requireActivity(),
                    transactionId = response.txId,
                    paymentId = response.paymentId
                )
            }

            override fun onFail(response: PaymentResponse.Fail) {
                onErrorInvoked(
                    code = response.code,
                )
            }
        })

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
            route = requireArguments().getString(ARG_ROUTE).orEmpty(),
        )
        collectEvents()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(
        route: String,
    ) {
        try {
            lifecycleScope.launch {
                delay(7000L)
                if (isLoading) {
                    showSimpleDialog(
                        title = "네트워크 연결 실패",
                        message = "서버가 불안정합니다.\n잠시 후 다시 시도해주세요."
                    )
                    isLoading = false
                }
            }
            binding.apply {
                cookieManager.apply {
                    this.acceptCookie()
                    this.acceptThirdPartyCookies(webView)
                }
                webView.apply {
                    settings.apply {
                        javaScriptEnabled = true
                        setSupportZoom(false)
                        builtInZoomControls = false
                        displayZoomControls = false
                        allowContentAccess = true
                        domStorageEnabled = true
                        val kloudWebChromeClient = object: WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                Log.d("WebAppInterface", "onConsoleMessage: ${consoleMessage?.message()}")
                                return super.onConsoleMessage(consoleMessage)
                            }
                        }
                        webChromeClient = kloudWebChromeClient
                        val customWebViewClient =
                            CustomWebViewClient(object : WebViewListener {
                                override fun onConnectSuccess() {
                                    isLoading = false
                                }

                                override fun onConnectFail() {

                                }

                                override fun onPageFinished() {
                                    cookieManager.flush()
                                }
                            })
                        addJavascriptInterface(WebAppInterface(object : EventReceiver {
                            override fun showToast(message: String) {
                                toast?.cancel()
                                toast =
                                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
                                toast?.show()
                            }

                            override fun replace(route: String) {
                                initWebView(route)
                            }

                            override fun push(route: String) {
                                navigate(route)
                            }

                            override fun pushAndAllClear(route: String) {
                                navigate(route, true)
                            }

                            override fun back() {
                                requireActivity().finish()
                            }

                            override fun navigateMain(bootInfo: String) {
                                Log.d("WebAppInterface", "navigateMain: $bootInfo")
                                launchMainScreen(bootInfo)
                            }

                            override fun clearToken() {
                                cookieManager.removeAllCookies(null)
                            }

                            override fun sendHapticFeedback() {
                                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            }

                            override fun sendKakaoLogin() {
                                viewModel.kakaoLogin(requireContext())
                            }

                            override fun sendGoogleLogin(configuration: GoogleLoginConfiguration) {
                                Log.d("WebAppInterface", "sendGoogleLogin: $configuration")
                                viewModel.googleLogin(
                                    context = requireContext(),
                                    serverClientId = configuration.serverClientId,
                                    nonce = configuration.nonce,
                                )
                            }

                            override fun showDialog(dialogInfo: KloudDialogInfo) {
                                Log.d("WebAppInterface", "showDialog: $dialogInfo")
                                try {
                                    KloudDialog.newInstance(
                                        id = dialogInfo.id.orEmpty(),
                                        route = dialogInfo.route,
                                        hideForeverMessage = dialogInfo.hideForeverMessage,
                                        imageUrl = dialogInfo.imageUrl,
                                        imageRatio = dialogInfo.imageRatio,
                                        onClick = {
                                            binding.webView.onDialogConfirm(
                                                activity = requireActivity(),
                                                dialogInfo = it
                                            )
                                        },
                                        onClickHideDialog = { id, clicked ->
                                            binding.webView.onHideDialog(
                                                activity = requireActivity(),
                                                dialogId = id,
                                                clicked = clicked,
                                            )
                                        },
                                        title = dialogInfo.title,
                                        message = dialogInfo.message,
                                        type = dialogInfo.type,
                                        ctaButtonText = dialogInfo.ctaButtonText,
                                    ).show(childFragmentManager, "KloudDialog")
                                } catch (e: Throwable) {
                                    Log.d("WebAppInterface", "showDialog: $e")
                                }
                            }

                            override fun showBottomSheet(bottomSheetInfo: String) {
                                TODO("Not yet implemented")
                            }

                            override fun requestPayment(command: String) {
                                val paymentInfo = Gson().fromJson(command, PaymentInfo::class.java)
                                requestPayment(paymentInfo)
                            }
                        }), "KloudEvent")
                        webViewClient = customWebViewClient
                        loadUrl(KloudWebUrlProvider.getUrl(requireContext(), route))
//                        loadUrl("http://192.168.45.154:3000$route")
                    }
                }
            }
        } catch (e: Exception) {
            isLoading = false
            Log.d("WebAppInterface", "initWebView: $e")
        }
    }

    override fun onResume() {
        super.onResume()

    }

    private fun navigate(route: String, withClear: Boolean = false) {
        val intent = Intent(requireActivity(), WebViewActivity::class.java).apply {
            if (withClear) {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        intent.putExtra("route", route)
        startActivity(intent)
        if (withClear) {
            requireActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down)

        }
    }

    private fun launchMainScreen(bootInfo: String) {
        val intent = Intent(requireActivity(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra("bootInfo", bootInfo)
        startActivity(intent)

    }

    private fun collectEvents() {
        lifecycleScope.launch {
            viewModel.onKakaoLoginSuccess.collect {
                binding.webView.onKakaoLoginSuccess(requireActivity(), it)
            }
        }

        lifecycleScope.launch {
            viewModel.onGoogleLoginSuccess.collect {
                binding.webView.onGoogleLoginSuccess(requireActivity(), it)
            }
        }

        lifecycleScope.launch {
            viewModel.errorInvoked.collect {
                Log.d("WebAppInterface", "collectEvents: ${it.message}")
            }
        }
    }

    private fun requestPayment(paymentInfo: PaymentInfo) {
        Log.d("WebAppInterface", "requestPayment: $paymentInfo")
        try {
            PortOne.requestPayment(
                requireActivity(),
                request = PaymentRequest(
                    storeId = paymentInfo.storeId,
                    channelKey = paymentInfo.channelKey,
                    paymentId = paymentInfo.paymentId,
                    orderName = paymentInfo.orderName,
                    amount = Amount(total = paymentInfo.price, currency = Currency.KRW), // 금액
                    method = PaymentMethod.Card(), // 결제수단 관련 정보
                    customer = Customer(
                        name = Customer.Name.Full(paymentInfo.userId),
                    )
                ),
                resultLauncher = paymentActivityResultLauncher
            )
        } catch (e: Throwable) {
            Log.d("WebAppInterface", "requestPayment: ${e.message}")
        }
    }

    private fun showSimpleDialog(title: String, message: String?) {
        val dialog = KloudDialog.newInstance(
            id = "Error",
            type = KloudDialogType.SIMPLE.name,
            title = title,
            message = message,
            onClickHideDialog = { id, clicked ->
            },
            onClick = {
            }
        )
        dialog.show(childFragmentManager, "KloudDialog")
    }

    private fun onErrorInvoked(code: String) {
        binding.webView.onErrorInvoked(
            requireActivity(),
            code = code,
        )
    }

    companion object {
        private const val ARG_ROUTE = "ARG_ROUTE"
        fun newInstance(
            route: String,
        ) = WebViewFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ROUTE, route)
            }
        }
    }
}