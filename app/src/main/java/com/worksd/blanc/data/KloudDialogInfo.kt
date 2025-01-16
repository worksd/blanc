package com.worksd.blanc.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class KloudDialogInfo(
    @SerializedName("route")
    val route: String,
    @SerializedName("hideForeverMessage")
    val hideForeverMessage: String?,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("imageRatio")
    val imageRatio: Float?,
)