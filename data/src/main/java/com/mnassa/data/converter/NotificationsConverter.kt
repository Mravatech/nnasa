package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.convert
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.bean.firebase.*
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.impl.NotificationExtraImpl
import com.mnassa.domain.model.impl.NotificationModelImpl
import timber.log.Timber
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
                author = convertAuthor(input.extra?.author, converter),
                post = convertPost(input.extra?.post, converter),
                reffered = convertAuthor(input.extra?.reffered, converter),
                recommended = convertAuthor(input.extra?.recommended, converter),
                group = convertGroup(input.extra?.group, converter),
                eventName = input.extra?.eventName,
                ticketsPrice = input.extra?.ticketsPrice,
                totalPrice = input.extra?.totalPrice,
                attendee = input.extra?.attendee,
                event = convertEvent(input.extra?.event, converter),
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

    private fun convertEvent(input: Map<String, EventDbEntity>?, converter: ConvertersContext): EventModel? {
        if (input == null) return null
        val entity = input.values.first()
        entity.id = input.keys.first()
        return converter.convert(entity)
    }

    private fun convertAuthor(input: Map<String, ShortAccountDbEntity>?, converter: ConvertersContext): ShortAccountModel? {
        if (input == null) return null
        val entity = input.values.first()
        entity.id = input.keys.first()
        return converter.convert(entity)
    }

    private fun convertPost(input: Map<String, PostDbEntity>?, converter: ConvertersContext): PostModel? {
        if (input == null) return null
        val entity = input.values.first()
        entity.id = input.keys.first()
        return converter.convert(entity, PostAdditionInfo(emptySet()), PostModel::class.java)
    }

    private fun convertGroup(input: Map<String, GroupDbEntity>?, converter: ConvertersContext): GroupModel? {
        if (input == null) return null
        val entity = input.values.first()
        entity.id = input.keys.first()
        return converter.convert(entity)
    }

}