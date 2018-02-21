package com.mnassa.data.network.bean

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 2/21/2018.
 */
class UserNetworkBean(
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String
)