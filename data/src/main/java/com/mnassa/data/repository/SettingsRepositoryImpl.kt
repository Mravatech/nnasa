package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.network.api.FirebaseSettingsApi
import com.mnassa.data.network.bean.firebase.PushSettingDbEntity
import com.mnassa.data.network.bean.retrofit.request.PushSettingsRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.data.repository.DatabaseContract.ACCOUNTS_PUSH_SETTINGS
import com.mnassa.data.repository.DatabaseContract.TABLE_CLIENT_DATA
import com.mnassa.data.repository.DatabaseContract.TABLE_CLIENT_DATA_PUSH_TYPES
import com.mnassa.domain.model.PushSettingModel
import com.mnassa.domain.model.impl.PushSettingModelImpl
import com.mnassa.domain.repository.SettingsRepository
import com.mnassa.domain.repository.UserRepository
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/19/2018
 */
class SettingsRepositoryImpl(private val db: DatabaseReference,
                             private val userRepository: UserRepository,
                             private val firebaseSettingsApi: FirebaseSettingsApi,
                             private val exceptionHandler: ExceptionHandler,
                             private val converter: ConvertersContext) : SettingsRepository {


    override suspend fun getUserPushSettings(): List<PushSettingModel> {
        val userAid = userRepository.getAccountIdOrNull()
        val userSettings = db
                .child(ACCOUNTS_PUSH_SETTINGS)
                .child(userAid)
                .awaitList<PushSettingDbEntity>(exceptionHandler)
        var settings = db.child(TABLE_CLIENT_DATA)
                .child(TABLE_CLIENT_DATA_PUSH_TYPES)
                .awaitList<PushSettingDbEntity>(exceptionHandler)
        val ids = userSettings.map { it.id }.toSet()
        settings = settings.filter { it.id !in ids }
        var result = settings + userSettings
        result = result.sortedBy { it.id }
        Timber.i(ids.toString())
        Timber.i(userSettings.toString())
        Timber.i(settings.toString())
        Timber.i(result.toString())
        return converter.convertCollection(result, PushSettingModel::class.java)
    }

    override suspend fun changeSetting(setting: PushSettingModel): List<PushSettingModel> {
        val notification = mapOf(setting.name to PushSettingsRequest(setting.isActive, setting.withSound))
        val result = firebaseSettingsApi.accountNotifications(notification).handleException(exceptionHandler)
        val keys = result.data.accountPushSettings.keys
        val settings = mutableListOf<PushSettingModel>()
        keys.forEach {
            val data = requireNotNull(result.data.accountPushSettings[it])
            settings.add(PushSettingModelImpl(
                    isActive = data.isActive,
                    withSound = data.withSound,
                    name = it
            ))
        }

        return settings

    }
}