package com.rawgraphy.blanc.data

import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PaymentInfo(
    @SerializedName("storeId")
    val storeId: String,
    @SerializedName("channelKey")
    val channelKey: String,
    @SerializedName("paymentId")
    val paymentId: String,
    @SerializedName("orderName")
    val orderName: String,
    @SerializedName("price")
    val price: Long,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("userName")
    val userName: String?,
    @SerializedName("userBirth")
    val userBirth: String?,
    @SerializedName("userPhone")
    val userPhone: String?,
    @SerializedName("customData")
    val customData: String?,
    @SerializedName("locale")
    val locale: String?,
    @SerializedName("pgProvider")
    val pgProvider: String?,
)