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
)