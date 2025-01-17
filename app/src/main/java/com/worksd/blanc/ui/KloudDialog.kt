package com.worksd.blanc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.worksd.blanc.R
import okhttp3.OkHttpClient

class KloudDialog : DialogFragment() {

    private var onClick: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val route = requireArguments().getString("route").orEmpty()
        val hideForeverMessage = requireArguments().getString("hideForeverMessage")
        val imageUrl = requireArguments().getString("imageUrl")
        val imageRatio = requireArguments().getFloat("imageRatio")
        return ComposeView(requireContext()).apply {
            setContent {
                KloudDialogScreen(
                    route = route,
                    hideForeverMessage = hideForeverMessage,
                    imageUrl = imageUrl,
                    imageRatio = imageRatio,
                    onDismissRequest = {
                        dismiss()
                    },
                    onClick = {
                        dismiss()
                        onClick?.invoke(route)
                    }
                )
            }
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (checkPreviousBottomSheet(manager, tag)) {
            super.show(manager, tag)
        }
    }

    private fun checkPreviousBottomSheet(manager: FragmentManager, tag: String?): Boolean {
        return manager.findFragmentByTag(tag) as? KloudDialog? == null
    }

    companion object {
        fun newInstance(
            route: String,
            hideForeverMessage: String?,
            imageUrl: String?,
            imageRatio: Float?,
            onClick: (String) -> Unit
        ): KloudDialog {
            val dialog = KloudDialog()
            dialog.arguments = Bundle().apply {
                putString("route", route)
                putString("hideForeverMessage", hideForeverMessage)
                putString("imageUrl", imageUrl)
                putFloat("imageRatio", imageRatio ?: 1f)
            }
            dialog.onClick = onClick
            return dialog
        }
    }
}
@Composable
private fun KloudDialogScreen(
    route: String,
    hideForeverMessage: String?,
    imageUrl: String?,
    imageRatio: Float?,
    onDismissRequest: () -> Unit,
    onClick: (String) -> Unit,
) {
    val context = LocalContext.current
    val isHideForeverClicked = remember { mutableStateOf(false) }

    // ImageLoader를 remember로 캐시
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = {
                            OkHttpClient()
                        }
                    )
                )
            }
            .build()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEFEF))
    ) {
        Image(
            modifier = Modifier
                .align(Alignment.End)
                .padding(10.dp)
                .pointerInput(Unit) {
                    detectTapGestures {
                        onDismissRequest()
                    }
                },
            painter = painterResource(R.drawable.ic_close_black),
            contentDescription = "Close Icon",
        )

        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(imageRatio ?: 1f)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            onClick(route)
                        }
                    },
                imageLoader = imageLoader,  // 캐시된 ImageLoader 사용
                model = imageUrl,
                contentDescription = "Dialog Image",
                contentScale = ContentScale.Crop,
            )
        }

        if (hideForeverMessage != null) {
            HideForeverRow(
                modifier = Modifier.align(Alignment.End),
                message = hideForeverMessage,
                isChecked = isHideForeverClicked.value,
                onCheckedChange = { isHideForeverClicked.value = it }
            )
        }
    }
}

@Composable
private fun HideForeverRow(
    modifier: Modifier = Modifier,
    message: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colorFilter = if (isChecked) Color(0xFF000000) else Color(0xFFD9D9D9)

    Row(
        modifier = modifier
            .padding(10.dp)
            .pointerInput(isChecked) {
                detectTapGestures {
                    onCheckedChange(!isChecked)
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Image(
            painter = painterResource(
                when (isChecked) {
                    true -> R.drawable.ic_checkbox_filled
                    false -> R.drawable.ic_checkbox_empty
                }
            ),
            contentDescription = "Hide Forever Checkbox",
        )
        Text(
            text = message,
            color = colorFilter,
            fontWeight = if (isChecked) FontWeight.Bold else null
        )
    }
}