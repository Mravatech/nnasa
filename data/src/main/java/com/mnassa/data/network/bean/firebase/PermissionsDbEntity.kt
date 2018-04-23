package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.PermissionsModel

/**
 * Created by Peter on 4/23/2018.
 */
data class PermissionsDbEntity(
        @SerializedName("createAccountPost") override val canCreateAccountPost: Boolean,
        @SerializedName("createEvent") override val canCreateEvent: Boolean,
        @SerializedName("createGeneralPost") override val canCreateGeneralPost: Boolean,
        @SerializedName("createNeedPost") override val canCreateNeedPost: Boolean,
        @SerializedName("createOfferPost") override val canCreateOfferPost: Boolean,
        @SerializedName("promoteEvent") override val canPromoteEvent: Boolean,
        @SerializedName("promoteGeneralPost") override val canPromoteGeneralPost: Boolean,
        @SerializedName("promoteNeedPost") override val canPromoteNeedPost: Boolean,
        @SerializedName("promoteOfferPost") override val canPromoteOfferPost: Boolean
) : PermissionsModel