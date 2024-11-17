package com.worksd.blanc.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BootInfoResponse(
    @SerializedName("bottomMenuList")
    val bottomMenuList: List<BottomMenuResponse>,
    @SerializedName("route")
    val route: String,
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
    @SerializedName("page")
    val page: PageInitResponse,
)

@Keep
data class PageInitResponse(
    @SerializedName("route")
    val route: String,
    @SerializedName("initialColor")
    val initialColor: String
)