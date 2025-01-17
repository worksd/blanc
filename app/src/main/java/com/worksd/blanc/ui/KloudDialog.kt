package com.worksd.blanc.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
        val id = requireArguments().getString("id").orEmpty()
        val route = requireArguments().getString("route").orEmpty()
        val hideForeverMessage = requireArguments().getString("hideForeverMessage")
        val imageUrl = requireArguments().getString("imageUrl")
        val imageRatio = requireArguments().getFloat("imageRatio")
        val title = requireArguments().getString("title")
        val body = requireArguments().getString("body")
        val withBackArrow = requireArguments().getBoolean("withBackArrow", false)
        val withConfirmButton = requireArguments().getBoolean("withConfirmButton", false)
        val withCancelButton = requireArguments().getBoolean("withCancelButton", false)

        return ComposeView(requireContext()).apply {
            setContent {
                KloudDialogScreen(
                    id = id,
                    hideForeverMessage = hideForeverMessage,
                    imageUrl = imageUrl,
                    imageRatio = imageRatio,
                    onDismissRequest = {
                        dismiss()
                    },
                    onClick = {
                        dismiss()
                        onClick?.invoke(route)
                    },
                    title = title,
                    body = body,
                    withBackArrow = withBackArrow,
                    withConfirmButton = withConfirmButton,
                    withCancelButton = withCancelButton,
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
            id: String?,
            route: String?,
            title: String?,
            body: String?,
            hideForeverMessage: String?,
            imageUrl: String?,
            imageRatio: Float?,
            withBackArrow: Boolean?,
            withConfirmButton: Boolean?,
            withCancelButton: Boolean?,
            onClick: (String) -> Unit
        ): KloudDialog {
            val dialog = KloudDialog()
            dialog.arguments = Bundle().apply {
                putString("id", id)
                putString("route", route)
                putString("title", title)
                putString("body", body)
                putString("hideForeverMessage", hideForeverMessage)
                putString("imageUrl", imageUrl)
                putFloat("imageRatio", imageRatio ?: 1f)
                putBoolean("withBackArrow", withBackArrow ?: false)
                putBoolean("withConfirmButton", withConfirmButton ?: false)
                putBoolean("withCancelButton", withCancelButton ?: false)
            }
            dialog.onClick = onClick
            return dialog
        }
    }
}
@Composable
private fun KloudDialogScreen(
    id: String,
    title: String? = null,
    body: String? = null,
    withBackArrow: Boolean = false,
    withConfirmButton: Boolean = false,
    withCancelButton: Boolean = false,
    hideForeverMessage: String? = null,
    imageUrl: String? = null,
    imageRatio: Float? = null,
    onDismissRequest: () -> Unit,
    onClick: (String?) -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFEFEFEF))
            .padding(vertical = 30.dp, horizontal = 20.dp),
    ) {
        if (withBackArrow) {
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
        }

        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(imageRatio ?: 1f)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            onClick(id)
                        }
                    },
                imageLoader = ImageLoader.Builder(context)
                    .components {
                        add(
                            OkHttpNetworkFetcherFactory(
                                callFactory = {
                                    OkHttpClient()
                                }
                            )
                        )
                    }
                    .build(),
                model = imageUrl,
                contentDescription = "Dialog Image",
                contentScale = ContentScale.Crop,
            )
        }


        if (hideForeverMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            HideForeverRow(
                modifier = Modifier.align(Alignment.End),
                message = hideForeverMessage,
            )
        }

        if (!title.isNullOrEmpty()) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (!body.isNullOrEmpty()) {
            Text(
                text = body,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = Color(0xFF86898C)
            )
        }
        
        if (withConfirmButton || withCancelButton) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (withCancelButton) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        onClick = { onDismissRequest() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEFEFEF),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("취소")
                    }
                }

                if (withConfirmButton) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        onClick = {
                            onDismissRequest()
                            onClick(id)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("확인")
                    }
                }
            }
        }
    }
}

@Composable
private fun HideForeverRow(
    modifier: Modifier = Modifier,
    message: String,
) {
    val isHideForeverClicked = remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .pointerInput(isHideForeverClicked.value) {
                detectTapGestures {
                    isHideForeverClicked.value = !isHideForeverClicked.value
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Image(
            painter = painterResource(
                when (isHideForeverClicked.value) {
                    true -> R.drawable.ic_checkbox_filled
                    false -> R.drawable.ic_checkbox_empty
                }
            ),
            contentDescription = "Hide Forever Checkbox",
        )
        Text(
            text = message,
            color = when(isHideForeverClicked.value) {
                true -> Color.Black
                false -> Color(0xFF86898C)
            },
            fontWeight = if (isHideForeverClicked.value) FontWeight.Bold else null
        )
    }
}

@Preview
@Composable
private fun KloudSimpleDialogPreview() {
    KloudDialogScreen(
        id = "Simple",
        title = "이미 가입된 계정이 있습니다",
        withConfirmButton = true,
        onClick = {},
        onDismissRequest = {},
    )
}