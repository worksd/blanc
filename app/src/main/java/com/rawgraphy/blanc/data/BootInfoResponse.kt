package com.rawgraphy.blanc.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BootInfoResponse(
    @SerializedName("bottomMenuList")
    val bottomMenuList: List<BottomMenuResponse>,
    @SerializedName("route")
    val route: String?,
)

@Keep
data class BottomMenuResponse(
    @SerializedName("label")
    val label: String,
    @SerializedName("labelSize")
    val labelSize: Int,
    @SerializedName("selectedColor")
    val selectedColor: String,
    @SerializedName("unselectedColor")
    val unselectedColor: String,
    @SerializedName("iconUrl")
    val iconUrl: String,
    @SerializedName("iconSize")
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