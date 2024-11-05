package com.worksd.blanc.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BootInfoResponse(
    @SerializedName("minimum_version")
    val minimumVersion: String,
    @SerializedName("bottom_menu_list")
    val bottomMenuList: List<BottomMenuResponse>
)

@Keep
data class BottomMenuResponse(
    @SerializedName("label")
    val label: String,
    @SerializedName("label_size")
    val labelSize: Int,
    @SerializedName("label_color")
    val labelColor: String,
    @SerializedName("icon_url")
    val iconUrl: String,
    @SerializedName("icon_size")
    val iconSize: Int,
    @SerializedName("url")
    val url: String,
)