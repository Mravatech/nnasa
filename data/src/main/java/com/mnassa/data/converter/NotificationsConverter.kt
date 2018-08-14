package com.mnassa.data.converter

import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.ConvertersContextRegistrationCallback
import com.mnassa.core.converter.registerConverter
import com.mnassa.data.network.bean.firebase.*
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.impl.NotificationExtraImpl
import com.mnassa.domain.model.impl.NotificationModelImpl
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
class NotificationsConverter : ConvertersContextRegistrationCallback {

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertPostData)
    }

    private fun convertPostData(input: NotificationDbEntity, token: Any?, converter: ConvertersContext): NotificationModelImpl {
        val extra = NotificationExtraImpl(
                author = input.extra?.author?.parseObject<ShortAccountDbEntity>()?.let { converter.convert(it, ShortAccountModel::class.java) },
                post = input.extra?.post?.parseObject<PostDbEntity>()?.let { converter.convert(it, PostModel::class.java) },
                reffered = input.extra?.reffered?.parseObject<ShortAccountDbEntity>()?.let { converter.convert(it, ShortAccountModel::class.java) },
                recommended = input.extra?.recommended?.parseObject<ShortAccountDbEntity>()?.let { converter.convert(it, ShortAccountModel::class.java) },
                group = input.extra?.group?.parseObject<GroupDbEntity>()?.let { converter.convert(it, GroupModel::class.java) },
                eventName = input.extra?.eventName,
                ticketsPrice = input.extra?.ticketsPrice,
                totalPrice = input.extra?.totalPrice,
                attendee = input.extra?.attendee,
                event = input.extra?.event?.parseObject<EventDbEntity>()?.let { converter.convert(it, EventModel::class.java) },
                newInviteNumber = input.extra?.newInviteNumber
        )
        if (extra.author == null) {
            extra.author = extra.post?.author
        }
        return NotificationModelImpl(
                id = input.id,
                createdAt = Date(input.createdAt),
                text = input.text,
                type = input.type,
                extra = extra,
                isOld = true
        )
    }
}