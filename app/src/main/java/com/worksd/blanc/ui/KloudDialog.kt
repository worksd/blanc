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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import coil.compose.AsyncImage
import com.worksd.blanc.R

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
    val isHideForeverClicked = remember { mutableStateOf(false) }
    val colorFilter = remember(isHideForeverClicked.value) {
        when (isHideForeverClicked.value) {
            true -> Color(0xFF000000)
            false -> Color(0xFFD9D9D9)
        }
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
                model = imageUrl,
                contentDescription = "Dialog Image",
                contentScale = ContentScale.Crop,
            )
        }
        if (hideForeverMessage != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(
                        10.dp
                    )
                    .pointerInput(Unit) {
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
                    text = hideForeverMessage,
                    color = colorFilter,
                    fontWeight = when (isHideForeverClicked.value) {
                        true -> FontWeight.Bold
                        false -> null
                    }
                )
            }
        }

    }
}