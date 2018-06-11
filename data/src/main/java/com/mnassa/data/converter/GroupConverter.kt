package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.bean.firebase.GroupDbEntity
import com.mnassa.data.network.bean.firebase.GroupPermissionsEntity
import com.mnassa.data.network.bean.retrofit.request.CreateGroupRequest
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.GroupModelImpl
import com.mnassa.domain.model.impl.ShortAccountModelImpl
import com.mnassa.domain.repository.UserRepository

/**
 * Created by Peter on 6/4/2018.
 */
class GroupConverter(userRepositoryLazy: () -> UserRepository) : ConvertersContextRegistrationCallback {
    private val userRepository: UserRepository by lazy(userRepositoryLazy)

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertGroup)
        convertersContext.registerConverter(this::makeRequest)
    }

    private fun convertGroup(input: GroupDbEntity, token: Any?, converter: ConvertersContext): GroupModel {
        val currentUserId = userRepository.getAccountIdOrException()

        val creator = when {
            input.author != null -> converter.convert(input.author, ShortAccountModel::class.java)
            else -> ShortAccountModelImpl.EMPTY
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
                canCreateEvent = group.permissions.canCreateEvent,
                canCreateAccountPost = group.permissions.canCreateAccountPost,
                canCreateOfferPost = group.permissions.canCreateOfferPost,
                canCreateNeedPost = group.permissions.canCreateNeedPost,
                canCreateGeneralPost = group.permissions.canCreateGeneralPost
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