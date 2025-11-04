package com.rawgraphy.blanc.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.rawgraphy.blanc.R
import com.rawgraphy.blanc.client.CustomWebViewClient
import com.rawgraphy.blanc.client.EventReceiver
import com.rawgraphy.blanc.client.WebAppInterface
import com.rawgraphy.blanc.client.WebViewListener
import com.rawgraphy.blanc.client.onDialogConfirm
import com.rawgraphy.blanc.client.onErrorInvoked
import com.rawgraphy.blanc.client.onFcmTokenComplete
import com.rawgraphy.blanc.client.onGoogleLoginSuccess
import com.rawgraphy.blanc.client.onHideDialog
import com.rawgraphy.blanc.client.onKakaoLoginSuccess
import com.rawgraphy.blanc.client.onPaymentSuccess
import com.rawgraphy.blanc.data.GoogleLoginConfiguration
import com.rawgraphy.blanc.data.KloudDialogInfo
import com.rawgraphy.blanc.data.PaymentInfo
import com.rawgraphy.blanc.data.RouteInfo
import com.rawgraphy.blanc.databinding.FragmentWebViewBinding
import com.rawgraphy.blanc.util.KloudWebUrlProvider
import com.rawgraphy.blanc.util.PrefUtils
import com.rawgraphy.blanc.util.WebEndPointKey
import com.rawgraphy.blanc.util.refreshWebView
import dagger.hilt.android.AndroidEntryPoint
import io.portone.sdk.android.PortOne
import io.portone.sdk.android.payment.PaymentCallback
import io.portone.sdk.android.payment.PaymentRequest
import io.portone.sdk.android.payment.PaymentResponse
import io.portone.sdk.android.type.Amount
import io.portone.sdk.android.type.Currency
import io.portone.sdk.android.type.Customer
import io.portone.sdk.android.type.PaymentMethod
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WebViewFragment : Fragment() {

    private val cookieManager by lazy { CookieManager.getInstance() }
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var bottomSheet: KloudBottomSheetFragment

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            isEnabled = false
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private lateinit var binding: FragmentWebViewBinding
    private var isLoading: Boolean = true
    private var toast: Toast? = null

    private val paymentActivityResultLauncher =
        PortOne.registerForPaymentActivity(this, callback = object :
            PaymentCallback {
            override fun onSuccess(response: PaymentResponse.Success) {
                Log.d("onConsoleMessage", "onSuccess: $response")
                binding.webView.onPaymentSuccess(
                    requireActivity(),
                    transactionId = response.txId,
                    paymentId = response.paymentId
                )
            }

            override fun onFail(response: PaymentResponse.Fail) {
                Log.d("onConsoleMessage", "onPaymentFail ${response}")
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

        val route = requireArguments().getString(ARG_ROUTE).orEmpty()
        val isBottomMenu = requireArguments().getBoolean(ARG_IS_BOTTOM_MENU)
        collectEvents()

        if (!isBottomMenu) {
            initWebView(route)
        }

        initTopBar()

        onBottomMenuChanged(route)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressedCallback
        )
        collectRefreshEvent(route)
        collectViewModelRefreshEvent(route)

        Log.d("WebViewFragment", "onViewCreated: $tag")
    }

    private fun initTopBar() {
        val title = arguments?.getString(ARG_TITLE)
        val ignoreSafeArea = arguments?.getBoolean(ARG_SAFE_AREA, false) ?: false

        if (title == null) {
            binding.topBar.visibility = View.GONE
            return
        }

        binding.topBar.visibility = View.VISIBLE

        // ComposeView 생명주기 맞추기 (권장)
        binding.topBar.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )

        binding.topBar.setContent {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp, vertical = when (ignoreSafeArea) {
                                true -> 36.dp
                                false -> 18.dp
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        modifier = Modifier
                            .semantics { contentDescription = "Back" }
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                requireActivity().finish()
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFF000000),
                    )
                }
            }
        }
    }

    private fun onBottomMenuChanged(route: String) {
        lifecycleScope.launch {
            viewModel.currentSelectedBottomRoute.collect {
                if (it == route && !binding.webView.isVisible) {
                    initWebView(route)
                }
            }
        }
    }

    private fun collectViewModelRefreshEvent(route: String) {
        lifecycleScope.launch {
            viewModel.refresh.collect {
                Log.d("WebViewFragment", "collectViewModelRefreshEvent: $it")
                if (route.startsWith(it)) {
                    binding.webView.reload()
                    Log.d("FirebaseMessaging", "collectViewModelRefreshEvent: $route")
                }
            }
        }
    }


    private fun collectRefreshEvent(route: String) {
        lifecycleScope.launch {
            refreshWebView.collect { endpoints ->
                val shouldRefresh = endpoints.any { route.startsWith(it) }
                if (shouldRefresh) {
                    binding.webView.reload()
                    Log.d("FirebaseMessaging", "collectRefreshEvent: $route")
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(
        pageRoute: String,
    ) {
        try {
            binding.webView.visibility = View.VISIBLE
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
                        val kloudWebChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                Log.d(
                                    "WebAppInterface",
                                    "onConsoleMessage: ${consoleMessage?.message()}"
                                )
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

                            override fun openExternalBrowser(url: String) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }

                            override fun replace(route: String) {
                                initWebView(route)
                            }

                            override fun push(routeInfo: RouteInfo) {
                                navigate(routeInfo, withBottomUp = false)
                            }

                            override fun fullSheet(routeInfo: RouteInfo) {
                                navigate(routeInfo, withBottomUp = true)
                            }

                            override fun pushAndAllClear(routeInfo: RouteInfo) {
                                clearAndPush(routeInfo)
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
                                        confirmTitle = dialogInfo.confirmTitle,
                                        cancelTitle = dialogInfo.cancelTitle,
                                        customData = dialogInfo.customData,
                                    ).show(childFragmentManager, "KloudDialog")
                                } catch (e: Throwable) {
                                    Log.d("WebAppInterface", "showDialog: $e")
                                }
                            }

                            override fun showBottomSheet(route: String) {
                                Log.d("WebViewFragment", "show bottom sheet $pageRoute")
                                bottomSheet = KloudBottomSheetFragment.newInstance(route)
                                bottomSheet.show(childFragmentManager, "KloudBottomSheet")
                            }

                            override fun closeBottomSheet() {
                                Log.d("WebViewFragment", "close bottom sheet $pageRoute")
                                try {
                                    val dialog = parentFragment as? KloudBottomSheetFragment
                                    dialog?.let { sheet ->
                                        if (sheet.isAdded && !sheet.isRemoving) {
                                            sheet.dismiss()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("WebViewFragment", "closeBottomSheet error: ${e.message}")
                                }
                            }

                            override fun requestPayment(command: String) {
                                val paymentInfo = Gson().fromJson(command, PaymentInfo::class.java)
                                requestPayment(paymentInfo)
                            }

                            override fun sendFcmToken() {
                                try {
                                    FirebaseMessaging.getInstance().token.addOnCompleteListener(
                                        OnCompleteListener { task ->
                                            if (!task.isSuccessful) {
                                                return@OnCompleteListener
                                            }
                                            val token = task.result
                                            binding.webView.onFcmTokenComplete(
                                                requireActivity(),
                                                fcmToken = token,
                                                udid = "",
                                            )
                                        })
                                } catch (e: Exception) {
                                    Log.d("WebAppInterface", "sendFcmToken: $e")
                                }
                            }

                            override fun changeWebEndpoint(endpoint: String) {
                                PrefUtils(context).setString(WebEndPointKey, endpoint)
                            }

                            override fun refresh(endpoint: String) {
                                viewModel.refresh(endpoint)
                            }

                        }), "KloudEvent")

                        val pInfo =
                            context.packageManager.getPackageInfo(context.packageName, 0)
                        val version = pInfo.versionName

                        val newUserAgent =
                            "${settings.userAgentString} KloudNativeClient/${version}"
                        settings.userAgentString = newUserAgent
                        webViewClient = customWebViewClient
                        loadUrl(KloudWebUrlProvider.getUrl(requireContext(), pageRoute))
//                        loadUrl("http://192.168.45.206:3000$pageRoute")

                    }
                }
            }
        } catch (e: Exception) {
            isLoading = false
            Log.d("WebAppInterface", "initWebView: $e")
        }
    }

    private fun navigate(routeInfo: RouteInfo, withBottomUp: Boolean) {
        val intent = Intent(requireActivity(), WebViewActivity::class.java)
        intent.putExtra("route", routeInfo.route)
        intent.putExtra("title", routeInfo.title)
        intent.putExtra("ignoreSafeArea", routeInfo.ignoreSafeArea)
        startActivity(intent)
        if (withBottomUp) {
            requireActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
    }

    private fun clearAndPush(routeInfo: RouteInfo) {
        val intent = Intent(requireActivity(), WebViewActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra("route", routeInfo.route)
        intent.putExtra("title", routeInfo.title)
        intent.putExtra("ignoreSafeArea", routeInfo.ignoreSafeArea)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out)
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
            confirmTitle = "확인",
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
        private const val ARG_TITLE = "ARG_TITLE"
        private const val ARG_SAFE_AREA = "ARG_SAFE_AREA"
        private const val ARG_IS_BOTTOM_MENU = "ARG_IS_BOTTOM_MENU"
        fun newInstance(
            route: String,
            title: String?,
            ignoreSafeArea: Boolean,
            isBottomMenu: Boolean,
        ) = WebViewFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ROUTE, route)
                putString(ARG_TITLE, title)
                putBoolean(ARG_SAFE_AREA, ignoreSafeArea)
                putBoolean(ARG_IS_BOTTOM_MENU, isBottomMenu)
            }
        }
    }
}


fun Modifier.runIf(
    condition: Boolean,
    ifTrue: Modifier.() -> Modifier,
    ifFalse: (Modifier.() -> Modifier)? = null
): Modifier {
    return if (condition) {
        then(ifTrue(Modifier))
    } else if (ifFalse != null) {
        then(ifFalse(Modifier))
    } else {
        this
    }
}