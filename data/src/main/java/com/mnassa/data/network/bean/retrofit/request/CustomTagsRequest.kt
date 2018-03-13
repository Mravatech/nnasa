package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/13/2018
 */
data class CustomTagsRequest(
        @SerializedName("tags")
        val tags: List<String>)