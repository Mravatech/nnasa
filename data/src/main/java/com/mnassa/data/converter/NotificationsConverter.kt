package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.bean.firebase.NotificationDbEntity
import com.mnassa.domain.model.AccountType
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
        val id = input.extra.author.keys.first()
        val shortAccountDbEntity = requireNotNull(input.extra.author[id])
        return NotificationModelImpl(
                createdAt = Date(input.createdAt),
                text = input.text,
                type = input.type,
                extra = NotificationExtraImpl(
                        id = id,
                        firebaseUserId = "",
                        userName = shortAccountDbEntity.userName,
                        accountType = AccountType.ORGANIZATION,
                        avatar = shortAccountDbEntity.avatar,
                        contactPhone = null,
                        language = null,
                        personalInfo = null,
                        organizationInfo = null,
                        abilities = emptyList()
                ),
                isOld = true
//                extra = null
        )
    }


}