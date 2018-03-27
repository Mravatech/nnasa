package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.convert
import com.androidkotlincore.entityconverter.registerConverter
import com.google.gson.Gson
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.bean.firebase.PostCountersDbEntity
import com.mnassa.data.network.bean.firebase.PostDbEntity
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.response.PostData
import com.mnassa.data.network.bean.retrofit.response.RepostData
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_PRIVACY_TYPE_PRIVATE
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_PRIVACY_TYPE_PUBLIC
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_PRIVACY_TYPE_WORLD
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_TYPE_ACCOUNT
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_TYPE_GENERAL
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_TYPE_NEED
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_TYPE_OFFER
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.PostCountersImpl
import com.mnassa.domain.model.impl.PostImpl
import java.util.*

/**
 * Created by Peter on 3/15/2018.
 */
class PostConverter : ConvertersContextRegistrationCallback {
    private val gson = Gson()

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertPost)
        convertersContext.registerConverter(this::convertNewsFeedItemCounters)
        convertersContext.registerConverter(this::convertPostType)
        convertersContext.registerConverter(this::convertPostPrivacyType)
        convertersContext.registerConverter(this::convertItemType)
        convertersContext.registerConverter(this::convertPostData)
        convertersContext.registerConverter(this::convertRePostData)
    }

    private fun convertPostData(input: PostData, token: Any?, converter: ConvertersContext): PostImpl {
        val post = input.post
        val id = input.id
        post.id = id
        return converter.convert(post)
    }

    private fun convertRePostData(input: RepostData, token: Any?, converter: ConvertersContext): PostImpl {
        val post = input.post
        val id = input.id
        post.id = id
        return converter.convert(post)
    }

    private fun convertPost(input: PostDbEntity, token: Any?, converter: ConvertersContext): PostImpl {

        return PostImpl(
                id = input.id,
                allConnections = input.allConnections,
                type = converter.convert(input.type),
                createdAt = Date(input.createdAt),
                images = input.images ?: emptyList(),
                locationPlace = input.location?.takeIf { it.en != null && it.ar != null }?.let {
                    converter.convert<LocationPlaceModel>(it)
                },
                originalCreatedAt = Date(input.originalCreatedAt),
                originalId = input.originalId,
                privacyConnections = input.privacyConnections ?: emptyList(),
                privacyType = converter.convert(input.privacyType),
                tags = input.tags ?: emptyList(),
                text = input.text,
                updatedAt = Date(input.updatedAt),
                counters = converter.convert(input.counters),
                author = convertAuthor(input.author, converter),
                copyOwnerId = input.copyOwner,
                price = input.price ?: 0.0,
                autoSuggest = input.autoSuggest ?: PostAutoSuggest.EMPTY
        )
    }

    private fun convertAuthor(input: Map<String, ShortAccountDbEntity>, converter: ConvertersContext): ShortAccountModel {
        val entity = input.values.first()
        entity.id = input.keys.first()

        return converter.convert(entity, ShortAccountModel::class.java)
    }

    private fun convertPostType(input: String): PostType {
        return when (input) {
            NEWS_FEED_TYPE_NEED -> PostType.NEED
            NEWS_FEED_TYPE_ACCOUNT -> PostType.PROFILE
            NEWS_FEED_TYPE_OFFER -> PostType.OFFER
            NEWS_FEED_TYPE_GENERAL -> PostType.GENERAL

            else -> throw IllegalArgumentException("Wrong post item type $input")
        }
    }

    private fun convertPostPrivacyType(input: String): PostPrivacyType {
        return when (input) {
            NEWS_FEED_PRIVACY_TYPE_PUBLIC -> PostPrivacyType.PUBLIC
            NEWS_FEED_PRIVACY_TYPE_PRIVATE -> PostPrivacyType.PRIVATE
            NEWS_FEED_PRIVACY_TYPE_WORLD -> PostPrivacyType.WORLD

            else -> throw IllegalArgumentException("Wrong post privacy type $input")
        }
    }

    private fun convertItemType(input: String): ItemType {
        return when (input) {
            NetworkContract.ItemType.POST -> ItemType.POST
            NetworkContract.ItemType.EVENT -> ItemType.EVENT

            else -> throw IllegalArgumentException("Wrong post item type $input")
        }
    }

    private fun convertNewsFeedItemCounters(input: PostCountersDbEntity): PostCountersImpl {
        return PostCountersImpl(
                comments = input.comments,
                likes = input.likes,
                recommend = input.recommend,
                reposts = input.reposts,
                unreadResponse = input.unreadResponse,
                views = input.views
        )
    }
}