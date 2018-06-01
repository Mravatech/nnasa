package com.mnassa.domain.model

import android.net.Uri
import java.io.Serializable

/**
 * Created by Peter on 5/14/2018.
 */
interface GroupModel : Model {
    val name: String
    val description: String
    val type: GroupType
    val avatar: String?
    val creator: ShortAccountModel?
    val admins: List<String>
    val numberOfParticipants: Long
    val numberOfInvites: Long
    val website: String?
    val locationPlace: LocationPlaceModel?
    val tags: List<String>
    val permissions: GroupPermissions
}

sealed class GroupType : Serializable {
    class Public : GroupType()
    class Private : GroupType()
}

data class RawGroupModel(
        val id: String?,
        val title: String,
        val description: String?,
        val placeId: String?,
        val website: String?,
        val avatarToUpload: Uri?,
        val avatarUploaded: String?,
        val tags: List<TagModel>,

        val processedTags: List<String> = emptyList(),
        val permissions: GroupPermissions
) : Serializable

data class GroupPermissions(
        val canCreateAccountPost: Boolean,
        val canCreateEvent: Boolean,
        val canCreateGeneralPost: Boolean,
        val canCreateNeedPost: Boolean,
        val canCreateOfferPost: Boolean
) : Serializable {
    companion object {
        val ADMIN_PERMISSIONS = GroupPermissions(
                canCreateAccountPost = true,
                canCreateEvent = true,
                canCreateGeneralPost = true,
                canCreateNeedPost = true,
                canCreateOfferPost = true
        )
        val NO_PERMISSIONS = GroupPermissions(
                canCreateAccountPost = false,
                canCreateEvent = false,
                canCreateGeneralPost = false,
                canCreateNeedPost = false,
                canCreateOfferPost = false
        )
    }
}