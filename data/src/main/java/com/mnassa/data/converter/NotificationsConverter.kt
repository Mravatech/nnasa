package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.bean.firebase.NotificationDbEntity
import com.mnassa.data.network.bean.firebase.PostDbEntity
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
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
        Timber.i(input.toString())
        val extra = NotificationExtraImpl(
                convertAuthor(input.extra?.author, converter),
                convertPost(input.extra?.post, converter),
                convertAuthor(input.extra?.reffered, converter),
                convertAuthor(input.extra?.recommended, converter),
                input.extra?.eventName,
                input.extra?.ticketsPrice,
                input.extra?.totalPrice,
                input.extra?.attendee)
        return NotificationModelImpl(
                id = input.id,
                createdAt = Date(input.createdAt),
                text = input.text,
                type = input.type,
                extra = extra,
                isOld = true
        )
    }

    private fun convertAuthor(input: Map<String, ShortAccountDbEntity>?, converter: ConvertersContext): ShortAccountModel? {
        if (input == null) return null
        val entity = input.values.first()
        entity.id = input.keys.first()
        return converter.convert(entity, ShortAccountModel::class.java)
    }

    private fun convertPost(input: Map<String, PostDbEntity>?, converter: ConvertersContext): PostModel? {
        if (input == null) return null
        val entity = input.values.first()
        entity.id = input.keys.first()
        return converter.convert(entity, PostModel::class.java)
    }

}