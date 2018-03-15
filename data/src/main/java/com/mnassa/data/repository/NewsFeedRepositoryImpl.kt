package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.await
import com.mnassa.data.extensions.toValueChannelWithPagination
import com.mnassa.data.network.bean.firebase.NewsFeedItemDbEntity
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.data.repository.DatabaseContract.TABLE_NEWS_FEED
import com.mnassa.domain.model.NewsFeedItemModel
import com.mnassa.domain.repository.NewsFeedRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/15/2018.
 */
class NewsFeedRepositoryImpl(private val db: DatabaseReference,
                             private val userRepository: UserRepository,
                             private val exceptionHandler: ExceptionHandler,
                             private val converter: ConvertersContext) : NewsFeedRepository {

    override suspend fun loadAll(): ReceiveChannel<NewsFeedItemModel> {
        val userId = requireNotNull(userRepository.getAccountId())

        return db
                .child(TABLE_NEWS_FEED)
                .child(userId)
                .toValueChannelWithPagination<NewsFeedItemDbEntity, NewsFeedItemModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = converter.convertFunc(NewsFeedItemModel::class.java))
    }

    override suspend fun loadById(id: String): NewsFeedItemModel? {
        val userId = requireNotNull(userRepository.getAccountId())

        val dbEntity = db
                .child(TABLE_NEWS_FEED)
                .child(userId)
                .child(id)
                .await<NewsFeedItemDbEntity>(exceptionHandler)
        return dbEntity?.run { converter.convert(this) }
    }
}