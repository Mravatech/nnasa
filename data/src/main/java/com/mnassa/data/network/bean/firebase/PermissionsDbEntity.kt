package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.PermissionsModel

/**
 * Created by Peter on 4/23/2018.
 */
data class PermissionsDbEntity(
        @SerializedName("createAccountPost") override val canCreateAccountPost: Boolean = false,
        @SerializedName("createEvent") override val canCreateEvent: Boolean = false,
        @SerializedName("createGeneralPost") override val canCreateGeneralPost: Boolean = false,
        @SerializedName("createNeedPost") override val canCreateNeedPost: Boolean = false,
        @SerializedName("createOfferPost") override val canCreateOfferPost: Boolean = false,
        @SerializedName("promoteEvent") override val canPromoteEvent: Boolean = false,
        @SerializedName("promoteGeneralPost") override val canPromoteGeneralPost: Boolean = false,
        @SerializedName("promoteNeedPost") override val canPromoteNeedPost: Boolean = false,
        @SerializedName("promoteOfferPost") override val canPromoteOfferPost: Boolean = false,
        @SerializedName("promoteAccountPost") override val canPromoteAccountPost: Boolean = false,
        @SerializedName("createCommunity") override val canCreateGroup: Boolean = false
) : PermissionsModel {

    companion object {
        val EMPTY = PermissionsDbEntity()
    }

}