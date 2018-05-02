package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 5/2/2018.
 */
data class ResetCounterRequest(@SerializedName("type") val type: String)