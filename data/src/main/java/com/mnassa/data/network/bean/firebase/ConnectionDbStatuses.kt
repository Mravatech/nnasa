package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/30/2018
 */

data class ConnectionDbStatuses(
        @SerializedName("connectionsStatus")
        val connectionsStatus: String?
)