package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.PermissionsModel

/**
 * Created by Peter on 4/23/2018.
 */
data class PermissionsDbEntity(
        @SerializedName("createAccountPost") val createAccountPost: Boolean? = null,
        @SerializedName("createEvent") val createEvent: Boolean? = null,
        @SerializedName("createGeneralPost") val createGeneralPost: Boolean? = null,
        @SerializedName("createNeedPost") val createNeedPost: Boolean? = null,
        @SerializedName("createOfferPost") val createOfferPost: Boolean? = null,
        @SerializedName("promoteEvent") val promoteEvent: Boolean? = null,
        @SerializedName("promoteGeneralPost") val promoteGeneralPost: Boolean? = null,
        @SerializedName("promoteNeedPost") val promoteNeedPost: Boolean? = null,
        @SerializedName("promoteOfferPost") val promoteOfferPost: Boolean? = null,
        @SerializedName("promoteAccountPost") val promoteAccountPost: Boolean? = null,
        @SerializedName("createCommunity") val createGroup: Boolean? = null
) : PermissionsModel {
    override val canCreateAccountPost: Boolean
        get() = createAccountPost ?: false
    override val canCreateEvent: Boolean
        get() = createEvent ?: false
    override val canCreateGeneralPost: Boolean
        get() = createGeneralPost ?: false
    override val canCreateNeedPost: Boolean
        get() = createNeedPost ?: false
    override val canCreateOfferPost: Boolean
        get() = createOfferPost ?: false
    override val canPromoteEvent: Boolean
        get() = promoteEvent ?: false
    override val canPromoteGeneralPost: Boolean
        get() = promoteGeneralPost ?: false
    override val canPromoteNeedPost: Boolean
        get() = promoteNeedPost ?: false
    override val canPromoteOfferPost: Boolean
        get() = promoteOfferPost ?: false
    override val canPromoteAccountPost: Boolean
        get() = promoteAccountPost ?: false
    override val canCreateGroup: Boolean
        get() = createGroup ?: false

    companion object {
        val EMPTY = PermissionsDbEntity()
    }

}