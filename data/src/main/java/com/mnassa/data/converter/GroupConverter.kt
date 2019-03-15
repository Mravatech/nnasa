package com.mnassa.data.converter

import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.ConvertersContextRegistrationCallback
import com.mnassa.core.converter.registerConverter
import com.mnassa.data.network.bean.firebase.GroupDbEntity
import com.mnassa.data.network.bean.firebase.GroupPermissionsEntity
import com.mnassa.data.network.bean.retrofit.request.CreateGroupRequest
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.GroupModelImpl

/**
 * Created by Peter on 6/4/2018.
 */
class GroupConverter(userInteractorLazy: () -> UserProfileInteractor) : ConvertersContextRegistrationCallback {
    private val userRepository by lazy(userInteractorLazy)

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertGroup)
        convertersContext.registerConverter(this::makeRequest)
    }

    private fun convertGroup(input: GroupDbEntity, token: Any?, converter: ConvertersContext): GroupModel {
        val currentUserId = userRepository.getAccountIdOrException()

        val creator = when {
            input.author != null -> converter.convert(input.author, ShortAccountModel::class.java)
            else -> ShortAccountModel.EMPTY
        }

        val permissions = input.permissions?.let {
            GroupPermissions(
                    canCreateOfferPost = it.canCreateOfferPost,
                    canCreateNeedPost = it.canCreateNeedPost,
                    canCreateGeneralPost = it.canCreateGeneralPost,
                    canCreateEvent = it.canCreateEvent,
                    canCreateAccountPost = it.canCreateAccountPost)
        } ?: GroupPermissions.NO_PERMISSIONS

        return GroupModelImpl(
                id = input.id,
                name = input.title ?: "Unnamed group",
                description = input.description ?: "",
                avatar = input.avatar,
                type = GroupType.Private(),
                admins = input.admins
                        ?: (if (input.isAdmin == true) listOf(currentUserId) else emptyList()),
                numberOfParticipants = input.counters?.numberOfParticipants ?: 0L,
                numberOfInvites = input.counters?.numberOfInvites ?: 0L,
                creator = creator,
                website = input.website,
                locationPlace = input.location?.let { converter.convert(it, LocationPlaceModel::class.java) },
                tags = input.tags ?: emptyList(),
                permissions = permissions)
    }

    private fun makeRequest(group: RawGroupModel): CreateGroupRequest {
        val permissions = GroupPermissionsEntity(
                canCreateEventOrNull = group.permissions.canCreateEvent,
                canCreateAccountPostOrNull = group.permissions.canCreateAccountPost,
                canCreateOfferPostOrNull = group.permissions.canCreateOfferPost,
                canCreateNeedPostOrNull = group.permissions.canCreateNeedPost,
                canCreateGeneralPostOrNull = group.permissions.canCreateGeneralPost
        )

        return CreateGroupRequest(
                communityId = group.id,
                id = group.id,
                description = group.description,
                title = group.title,
                website = group.website,
                location = group.placeId,
                avatar = group.avatarUploaded,
                tags = group.processedTags.takeIf { it.isNotEmpty() },
                permissions = permissions
        )
    }
}