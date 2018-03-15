package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.convert
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.bean.firebase.NewsFeedItemCountersDbEntity
import com.mnassa.data.network.bean.firebase.NewsFeedItemDbEntity
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_PRIVACY_TYPE_PRIVATE
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_PRIVACY_TYPE_PUBLIC
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_TYPE_ACCOUNT
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_TYPE_GENERAL
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_TYPE_NEED
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_TYPE_OFFER
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.NewsFeedItemCountersImpl
import com.mnassa.domain.model.impl.NewsFeedItemModelImpl
import java.util.*

/**
 * Created by Peter on 3/15/2018.
 */
class NewsFeedConverter : ConvertersContextRegistrationCallback {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertNewsFeedItem)
        convertersContext.registerConverter(this::convertNewsFeedItemCounters)
    }

    private fun convertNewsFeedItem(input: NewsFeedItemDbEntity, token: Any?, converter: ConvertersContext): NewsFeedItemModelImpl {

        return NewsFeedItemModelImpl(
                id = input.id,
                allConnections = input.allConnections,
                type = convertNewsFeedItemType(input.type),
                createdAt = Date(input.createdAt),
                images = input.images ?: emptyList(),
                locationPlace = input.location?.let { converter.convert<LocationPlaceModel>(it) },
                originalCreatedAt = Date(input.originalCreatedAt),
                originalId = input.originalId,
                privacyConnections = input.privacyConnections ?: emptyList(),
                privacyType = convertNewsFeedItemPrivacyType(input.privacyType),
                tags = input.tags ?: emptyList(),
                text = input.text,
                updatedAt = Date(input.updatedAt),
                counters = converter.convert(input.counters),
                author = convertAuthor(input.author, converter),
                copyOwnerId = input.copyOwner
        )
    }

    private fun convertAuthor(input: Map<String, ShortAccountDbEntity>, converter: ConvertersContext): ShortAccountModel {
        val entity = input.values.first()
        entity.id = input.keys.first()

        return converter.convert(entity, ShortAccountModel::class.java)
    }

    private fun convertNewsFeedItemType(input: String): NewsFeedItemType {
        return when (input) {
            NEWS_FEED_TYPE_NEED -> NewsFeedItemType.NEED
            NEWS_FEED_TYPE_ACCOUNT -> NewsFeedItemType.PROFILE
            NEWS_FEED_TYPE_OFFER -> NewsFeedItemType.OFFER
            NEWS_FEED_TYPE_GENERAL -> NewsFeedItemType.GENERAL

            else -> throw IllegalArgumentException("Wrong news feed item type $input")
        }
    }

    private fun convertNewsFeedItemPrivacyType(input: String): NewsFeedItemPrivacyType {
        return when (input) {
            NEWS_FEED_PRIVACY_TYPE_PUBLIC -> NewsFeedItemPrivacyType.PUBLIC
            NEWS_FEED_PRIVACY_TYPE_PRIVATE -> NewsFeedItemPrivacyType.PRIVATE

            else -> throw IllegalArgumentException("Wrong news feed item privacy type $input")
        }
    }

    private fun convertNewsFeedItemCounters(input: NewsFeedItemCountersDbEntity): NewsFeedItemCountersImpl {
        return NewsFeedItemCountersImpl(
                comments = input.comments,
                likes = input.likes,
                recommend = input.recommend,
                reposts = input.reposts,
                unreadResponse = input.unreadResponse,
                views = input.views
        )
    }
}