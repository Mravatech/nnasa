package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 4/30/2018.
 */
data class HideInfoPostRequest(@SerializedName("postId") val postId: String)