package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 6/19/2018.
 */
data class AddTagsDialogShowingTimeRequest(
        @SerializedName("id") val id: String,
        @SerializedName("tagReminderShowedAt") val tagReminderShowedAt: Long
)