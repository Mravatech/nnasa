package com.mnassa.data.network.bean.retrofit.response

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/13/2018
 */
data class CustomTagsResponse(
        @SerializedName("data")
        val data: CustomTags)

data class CustomTags(
        @SerializedName("tagKeys")
        val tags: List<String>)