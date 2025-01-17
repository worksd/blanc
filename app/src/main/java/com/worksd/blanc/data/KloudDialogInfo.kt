package com.worksd.blanc.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class KloudDialogInfo(
    @SerializedName("id")
    val id: String?,
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
    @SerializedName("body")
    val body: String?,
    @SerializedName("withBackArrow")
    val withBackArrow: Boolean?,
    @SerializedName("withConfirmButton")
    val withConfirmButton: Boolean?,
    @SerializedName("withCancelButton")
    val withCancelButton: Boolean?,
)