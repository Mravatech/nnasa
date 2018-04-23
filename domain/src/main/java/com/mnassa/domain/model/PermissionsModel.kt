package com.mnassa.domain.model

/**
 * Created by Peter on 4/23/2018.
 */
interface PermissionsModel {
    val canCreateAccountPost: Boolean
    val canCreateEvent: Boolean
    val canCreateGeneralPost: Boolean
    val canCreateNeedPost: Boolean
    val canCreateOfferPost: Boolean
    //
    val canPromoteEvent: Boolean
    val canPromoteGeneralPost: Boolean
    val canPromoteNeedPost: Boolean
    val canPromoteOfferPost: Boolean
}