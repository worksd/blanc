package com.rawgraphy.blanc.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class KloudDialogInfo(
    @SerializedName("id")
    val id: String?,
    @SerializedName("type")
    val type: String,
    @SerializedName("route")
    val route: String?,
    @SerializedName("hideForeverMessage")
    val hideForeverMessage: String?,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("imageRatio")
    val imageRatio: Float?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("ctaButtonText")
    val ctaButtonText: String?,
    @SerializedName("confirmTitle")
    val confirmTitle: String?,
    @SerializedName("cancelTitle")
    val cancelTitle: String?,
    @SerializedName("customData")
    val customData: String?,
)