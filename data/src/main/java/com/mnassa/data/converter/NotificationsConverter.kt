package com.mnassa.data.converter

import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.ConvertersContextRegistrationCallback
import com.mnassa.core.converter.registerConverter
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.bean.firebase.*
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.*
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
class NotificationsConverter : ConvertersContextRegistrationCallback {

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertPostData)
        convertersContext.registerConverter(this::convertNotificationPost)
        convertersContext.registerConverter(this::convertNotificationEvent)
        convertersContext.registerConverter(this::convertNotificationUserRecommended)
        convertersContext.registerConverter(this::convertNotificationUserReferred)
    }

    private fun convertPostData(input: NotificationDbEntity, token: Any?, converter: ConvertersContext): NotificationModelImpl {
        val extra = NotificationExtraImpl(
                author = input.extra?.author?.parseObject<ShortAccountDbEntity>()?.let { converter.convert(it, ShortAccountModel::class.java) },
                post = input.extra?.post?.parseObject<PostDbEntity>()?.let { converter.convert(it, NotificationExtra.Post::class.java) },
                reffered = input.extra?.reffered?.parseObject<ShortAccountDbEntity>()?.let { converter.convert(it, NotificationExtra.UserReferred::class.java) },
                recommended = input.extra?.recommended?.parseObject<ShortAccountDbEntity>()?.let { converter.convert(it, NotificationExtra.UserRecommended::class.java) },
                group = input.extra?.group?.parseObject<GroupDbEntity>()?.let { converter.convert(it, GroupModel::class.java) },
                eventName = input.extra?.eventName,
                ticketsPrice = input.extra?.ticketsPrice,
                totalPrice = input.extra?.totalPrice,
                attendee = input.extra?.attendee,
                event = input.extra?.event?.parseObject<EventDbEntity>()?.let { converter.convert(it, NotificationExtra.Event::class.java) },
                newInviteNumber = input.extra?.newInviteNumber
        )
        if (extra.author == null) {
            extra.author = extra.post?.author
        }
        return NotificationModelImpl(
                id = input.id,
                createdAt = Date(input.createdAt),
                text = input.text ?: CONVERT_ERROR_MESSAGE,
                type = input.type,
                extra = extra,
                isOld = true
        )
    }

    private fun convertNotificationPost(input: PostDbEntity, token: Any?, converter: ConvertersContext): NotificationExtraPostImpl {
        return NotificationExtraPostImpl(
            id = input.id,
            author = input.author.parseObject<ShortAccountDbEntity>()?.let { converter.convert(it, ShortAccountModel::class.java) },
            text = input.text ?: CONVERT_ERROR_MESSAGE,
            type = input.type
        )
    }

    private fun convertNotificationEvent(input: EventDbEntity, token: Any?, converter: ConvertersContext): NotificationExtraEventImpl {
        return NotificationExtraEventImpl(
            id = input.id,
            author = input.author.let { converter.convert(it, ShortAccountModel::class.java) },
            title = input.title ?: CONVERT_ERROR_MESSAGE
        )
    }

    private fun convertNotificationUserReferred(input: ShortAccountDbEntity, token: Any?, converter: ConvertersContext): NotificationExtraUserReferredImpl {
        var personalInfo: PersonalAccountDiffModel? = null
        var organizationInfo: OrganizationAccountDiffModel? = null
        when (input.type) {
            NetworkContract.AccountType.ORGANIZATION -> {
                organizationInfo = OrganizationAccountDiffModelImpl(
                    organizationName = requireNotNull(input.organizationName)
                )
            }
            NetworkContract.AccountType.PERSONAL -> {
                personalInfo = PersonalAccountDiffModelImpl(
                    firstName = requireNotNull(input.firstName),
                    lastName = requireNotNull(input.lastName)
                )
            }
            else -> throw IllegalArgumentException("Illegal account type ${input.type}")
        }

        return NotificationExtraUserReferredImpl(
            id = input.id,
            userName = input.userName ?: CONVERT_ERROR_MESSAGE,
            personalInfo = personalInfo,
            organizationInfo = organizationInfo
        )
    }

    private fun convertNotificationUserRecommended(input: ShortAccountDbEntity, token: Any?, converter: ConvertersContext): NotificationExtraUserRecommendedImpl {
        var personalInfo: PersonalAccountDiffModel? = null
        var organizationInfo: OrganizationAccountDiffModel? = null
        when (input.type) {
            NetworkContract.AccountType.ORGANIZATION -> {
                organizationInfo = OrganizationAccountDiffModelImpl(
                    organizationName = requireNotNull(input.organizationName)
                )
            }
            NetworkContract.AccountType.PERSONAL -> {
                personalInfo = PersonalAccountDiffModelImpl(
                    firstName = requireNotNull(input.firstName),
                    lastName = requireNotNull(input.lastName)
                )
            }
            else -> throw IllegalArgumentException("Illegal account type ${input.type}")
        }

        return NotificationExtraUserRecommendedImpl(
            id = input.id,
            userName = input.userName ?: CONVERT_ERROR_MESSAGE,
            personalInfo = personalInfo,
            organizationInfo = organizationInfo
        )
    }
}