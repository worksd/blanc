package com.worksd.blanc.data

import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class GoogleLoginConfiguration(
    @SerializedName("serverClientId")
    val serverClientId: String,
    @SerializedName("nonce")
    val nonce: String,
)