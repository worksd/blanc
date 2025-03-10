package com.rawgraphy.blanc.ui

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.sp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.rawgraphy.blanc.R
import com.rawgraphy.blanc.data.KloudDialogInfo
import okhttp3.OkHttpClient

enum class KloudDialogType {
    SIMPLE,
    IMAGE,
    YESORNO,
}

class KloudDialog : DialogFragment() {

    private var onClick: ((KloudDialogInfo) -> Unit)? = null
    private var onClickHideDialog: ((String, Boolean) -> Unit)? = null

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
        val message = requireArguments().getString("message")
        val type = requireArguments().getString("type")
        val ctaButtonText = requireArguments().getString("ctaButtonText")
        val confirmTitle = requireArguments().getString("confirmTitle")
        val cancelTitle = requireArguments().getString("cancelTitle")

        return ComposeView(requireContext()).apply {
            setContent {
                if (type == KloudDialogType.SIMPLE.name) {
                    SimpleDialogScreen(
                        id = id,
                        title = title.orEmpty(),
                        onDismissRequest = {
                            dismiss()
                        },
                        message = message,
                        confirmTitle = confirmTitle.orEmpty(),
                    )
                } else if (type == KloudDialogType.IMAGE.name) {
                    ImageDialogScreen(
                        id = id,
                        hideForeverMessage = hideForeverMessage,
                        imageUrl = imageUrl.orEmpty(),
                        imageRatio = imageRatio,
                        onDismissRequest = {
                            dismiss()
                        },
                        onClick = {
                            onClick?.invoke(
                                KloudDialogInfo(
                                    id = id,
                                    type = type,
                                    route = route,
                                    hideForeverMessage = hideForeverMessage,
                                    imageUrl = imageUrl,
                                    imageRatio = imageRatio,
                                    title = title,
                                    message = message,
                                    ctaButtonText = ctaButtonText,
                                    confirmTitle = confirmTitle,
                                    cancelTitle = cancelTitle,
                                )
                            )
                        },
                        onClickHideDialog = { dialogId, isHideForever ->
                            onClickHideDialog?.invoke(dialogId, isHideForever)
                        },
                        ctaButtonText = ctaButtonText,
                    )
                } else if (type == KloudDialogType.YESORNO.name) {
                    YesOrNoDialogScreen(
                        id = id,
                        title = title.orEmpty(),
                        message = message,
                        onConfirm = {
                            onClick?.invoke(
                                KloudDialogInfo(
                                    id = id,
                                    type = type,
                                    route = route,
                                    hideForeverMessage = hideForeverMessage,
                                    imageUrl = imageUrl,
                                    imageRatio = imageRatio,
                                    title = title,
                                    message = message,
                                    ctaButtonText = ctaButtonText,
                                    confirmTitle = confirmTitle,
                                    cancelTitle = cancelTitle,
                                )
                            )
                        },
                        onDismissRequest = {
                            dismiss()
                        },
                        confirmTitle = confirmTitle.orEmpty(),
                        cancelTitle = cancelTitle.orEmpty(),
                    )
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(android.R.color.transparent)))
        return dialog
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
            id: String,
            type: String,
            route: String? = null,
            title: String? = null,
            message: String? = null,
            hideForeverMessage: String? = null,
            ctaButtonText: String? = null,
            imageUrl: String? = null,
            imageRatio: Float? = null,
            confirmTitle: String? = null,
            cancelTitle: String? = null,
            onClick: (KloudDialogInfo) -> Unit,
            onClickHideDialog: (String, Boolean) -> Unit,
        ): KloudDialog {
            val dialog = KloudDialog()
            dialog.arguments = Bundle().apply {
                putString("id", id)
                putString("type", type)
                putString("route", route)
                putString("title", title)
                putString("message", message)
                putString("hideForeverMessage", hideForeverMessage)
                putString("imageUrl", imageUrl)
                putFloat("imageRatio", imageRatio ?: 1f)
                putString("ctaButtonText", ctaButtonText)
                putString("confirmTitle", confirmTitle)
                putString("cancelTitle", cancelTitle)
            }
            dialog.onClick = onClick
            dialog.onClickHideDialog = onClickHideDialog
            return dialog
        }
    }
}

@Composable
private fun ImageDialogScreen(
    id: String,
    imageUrl: String,
    imageRatio: Float,
    ctaButtonText: String? = null,
    hideForeverMessage: String? = null,
    onDismissRequest: () -> Unit,
    onClick: (String?) -> Unit,
    onClickHideDialog: (String, Boolean) -> Unit,
) {
    val context = LocalContext.current

    Column {
        if (hideForeverMessage != null) {
            HideForeverRow(
                modifier = Modifier.align(Alignment.Start),
                message = hideForeverMessage,
                onClickHideDialog = onClickHideDialog,
                id = id,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp),
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(imageRatio)
                    .clip(RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            onDismissRequest()
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
                placeholder = painterResource(R.drawable.ic_logo),
            )

            if (!ctaButtonText.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
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
                    Text(ctaButtonText)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            modifier = Modifier
                .size(44.dp)
                .pointerInput(Unit) {
                    detectTapGestures {
                        onDismissRequest()
                    }
                }
                .align(Alignment.CenterHorizontally),
            painter = painterResource(R.drawable.ic_circle_close),
            contentDescription = "Circle Close",
        )
    }
}

@Composable
private fun HideForeverRow(
    modifier: Modifier = Modifier,
    id: String,
    message: String,
    onClickHideDialog: (String, Boolean) -> Unit,
) {
    val isHideForeverClicked = remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .padding(start = 12.dp)
            .pointerInput(isHideForeverClicked.value) {
                detectTapGestures {
                    isHideForeverClicked.value = !isHideForeverClicked.value
                    onClickHideDialog(id, isHideForeverClicked.value)
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Image(
            painter = painterResource(
                when (isHideForeverClicked.value) {
                    true -> R.drawable.ic_check_filled
                    false -> R.drawable.ic_check
                }),
                contentDescription = "Hide Forever Checkbox",
            )
                    Text (
                    text = message,
            color = Color.White,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SimpleDialogScreen(
    id: String,
    title: String,
    message: String?,
    confirmTitle: String,
    onDismissRequest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEFEFEF))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Black,
            fontSize = 16.sp,
        )
        if (!message.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            onClick = {
                onDismissRequest()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(confirmTitle)
        }
    }
}

@Composable
private fun YesOrNoDialogScreen(
    id: String,
    title: String,
    message: String?,
    confirmTitle: String,
    cancelTitle: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEFEFEF))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Black,
            fontSize = 16.sp,
        )
        if (!message.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .border(1.dp, Color(0xFFECEEF1), RoundedCornerShape(8.dp)),
                onClick = {
                    onDismissRequest()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(cancelTitle)
            }
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                onClick = {
                    onConfirm()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(confirmTitle)
            }
        }
    }
}

@Preview
@Composable
private fun SimpleDialogPreview() {
    SimpleDialogScreen(
        id = "Simple",
        title = "이미 가입된 계정이 있습니다",
        message = "와이파이 확인해 임마!",
        onDismissRequest = {},
        confirmTitle = "확인",
    )
}

@Preview
@Composable
private fun YesOrNoDialogPreview() {
    YesOrNoDialogScreen(
        id = "Simple",
        title = "이미 가입된 계정이 있습니다",
        message = "와이파이 확인해 임마!",
        onConfirm = {},
        onDismissRequest = {},
        confirmTitle = "확인",
        cancelTitle = "취소",
    )
}


@Preview
@Composable
private fun ImageDialogPreview() {
    ImageDialogScreen(
        id = "Image",
        imageUrl = "sdf",
        imageRatio = 0.7f,
        onDismissRequest = {},
        ctaButtonText = "이벤트 바로가기",
        hideForeverMessage = "오늘 하루 보지않기",
        onClick = {},
        onClickHideDialog = { id, clicked -> }
    )
}