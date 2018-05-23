package com.mnassa.data.network.bean.retrofit.response

import com.google.gson.annotations.SerializedName
import com.mnassa.data.network.bean.firebase.GroupDbEntity

/**
 * Created by Peter on 5/23/2018.
 */
class CreateGroupResponse {
    @SerializedName("data")
    internal lateinit var group: GroupDbEntity
}