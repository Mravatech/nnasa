package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.convert
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.bean.firebase.OfferCategoryDbModel
import com.mnassa.data.network.bean.firebase.PostCountersDbEntity
import com.mnassa.data.network.bean.firebase.PostDbEntity
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.response.PostData
import com.mnassa.data.repository.DatabaseContract.EXPIRATION_TYPE_ACTIVE
import com.mnassa.data.repository.DatabaseContract.EXPIRATION_TYPE_CLOSED
import com.mnassa.data.repository.DatabaseContract.EXPIRATION_TYPE_EXPIRED
import com.mnassa.data.repository.DatabaseContract.EXPIRATION_TYPE_FULFILLED
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_PRIVACY_TYPE_PRIVATE
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_PRIVACY_TYPE_PUBLIC
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_PRIVACY_TYPE_WORLD
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_TYPE_ACCOUNT
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_TYPE_GENERAL
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_TYPE_INFO
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_TYPE_NEED
import com.mnassa.data.repository.DatabaseContract.NEWS_FEED_TYPE_OFFER
import com.mnassa.domain.exception.FirebaseMappingException
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.*
import com.mnassa.domain.other.LanguageProvider
import timber.log.Timber
import java.util.*

/**
 * Created by Peter on 3/15/2018.
 */
class PostConverter(private val languageProvider: LanguageProvider) : ConvertersContextRegistrationCallback {

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertPost)
        convertersContext.registerConverter(this::convertNewsFeedItemCounters)
        convertersContext.registerConverter(this::convertPostType)
        convertersContext.registerConverter(this::convertFromPostType)
        convertersContext.registerConverter(this::convertPostPrivacyType)
        convertersContext.registerConverter(this::convertItemType)
        convertersContext.registerConverter(this::convertPostData)
        convertersContext.registerConverter(this::convertPostDataToOffer)
        convertersContext.registerConverter(this::convertInfoPost)
        convertersContext.registerConverter(this::convertOfferPost)
        convertersContext.registerConverter(this::convertOfferCategory)
    }

    private fun convertPostData(input: PostData, token: Any?, converter: ConvertersContext): PostModelImpl {
        val post = input.post
        val id = input.id
        post.id = id
        return converter.convert(post)
    }

    private fun convertPostDataToOffer(input: PostData, token: Any?, converter: ConvertersContext): OfferPostModelImpl {
        return convertPostData(input, token, converter) as OfferPostModelImpl
    }

    private fun convertPost(input: PostDbEntity, token: Any?, converter: ConvertersContext): PostModelImpl {

        val attachments = input.images.orEmpty().mapIndexed { index, image ->
            val videoUrl = input.videos.orEmpty().getOrNull(index)
            if (videoUrl != null) PostAttachment.PostVideoAttachment(previewUrl = image, videoUrl = videoUrl)
            else PostAttachment.PostPhotoAttachment(image)
        }

        val postType: PostType = converter.convert(input.type)
        return when (postType) {
            is PostType.PROFILE -> RecommendedProfilePostModelImpl(
                    id = input.id,
                    allConnections = input.allConnections,
                    type = postType,
                    createdAt = Date(input.createdAt),
                    attachments = attachments,
                    locationPlace = input.location?.takeIf { it.en != null && it.ar != null }?.let {
                        converter.convert<LocationPlaceModel>(it)
                    },
                    originalCreatedAt = Date(input.originalCreatedAt),
                    originalId = input.originalId,
                    privacyConnections = input.privacyConnections?.toSet() ?: emptySet(),
                    privacyType = converter.convert(input.privacyType),
                    tags = input.tags ?: emptyList(),
                    text = input.text,
                    statusOfExpiration = convertExpiration(input.statusOfExpiration),
                    timeOfExpiration = input.timeOfExpiration?.let { Date(it) },
                    updatedAt = Date(input.updatedAt),
                    counters = converter.convert(input.counters),
                    author = convertAuthor(input.author, converter),
                    copyOwnerId = input.copyOwner,
                    price = input.price ?: 0.0,
                    autoSuggest = input.autoSuggest ?: PostAutoSuggest.EMPTY,
                    repostAuthor = input.repostAuthor?.run { convertAuthor(this, converter) },
                    recommendedProfile = convertAuthor(requireNotNull(input.postedAccount), converter),
                    offers = emptyList()
            )
            is PostType.INFO -> InfoPostImpl(
                    id = input.id,
                    allConnections = input.allConnections,
                    type = postType,
                    createdAt = Date(input.createdAt),
                    attachments = attachments,
                    locationPlace = input.location?.takeIf { it.en != null && it.ar != null }?.let {
                        converter.convert<LocationPlaceModel>(it)
                    },
                    originalCreatedAt = Date(input.originalCreatedAt),
                    originalId = input.originalId,
                    privacyConnections = input.privacyConnections?.toSet() ?: emptySet(),
                    privacyType = converter.convert(input.privacyType),
                    tags = input.tags ?: emptyList(),
                    text = input.text,
                    updatedAt = Date(input.updatedAt),
                    counters = converter.convert(input.counters),
                    author = convertAuthor(input.author, converter),
                    copyOwnerId = input.copyOwner,
                    price = input.price ?: 0.0,
                    autoSuggest = input.autoSuggest ?: PostAutoSuggest.EMPTY,
                    repostAuthor = input.repostAuthor?.run { convertAuthor(this, converter) },
                    statusOfExpiration = convertExpiration(input.statusOfExpiration),
                    timeOfExpiration = input.timeOfExpiration?.let { Date(it) },
                    title = input.title
                            ?: throw FirebaseMappingException("info post ${input.id}", RuntimeException("Title is NULL!"))
            )
            is PostType.OFFER -> OfferPostModelImpl(
                    id = input.id,
                    allConnections = input.allConnections,
                    type = postType,
                    createdAt = Date(input.createdAt),
                    attachments = attachments,
                    locationPlace = input.location?.takeIf { it.en != null && it.ar != null }?.let {
                        converter.convert<LocationPlaceModel>(it)
                    },
                    originalCreatedAt = Date(input.originalCreatedAt),
                    originalId = input.originalId,
                    privacyConnections = input.privacyConnections?.toSet() ?: emptySet(),
                    privacyType = converter.convert(input.privacyType),
                    tags = input.tags ?: emptyList(),
                    text = input.text,
                    updatedAt = Date(input.updatedAt),
                    counters = converter.convert(input.counters),
                    author = convertAuthor(input.author, converter),
                    copyOwnerId = input.copyOwner,
                    price = input.price ?: 0.0,
                    autoSuggest = input.autoSuggest ?: PostAutoSuggest.EMPTY,
                    repostAuthor = input.repostAuthor?.run { convertAuthor(this, converter) },
                    title = input.title
                            ?: throw FirebaseMappingException("offer post ${input.id}", RuntimeException("Title is NULL!")),
                    category = input.category,
                    subCategory = input.subcategory,
                    statusOfExpiration = convertExpiration(input.statusOfExpiration),
                    timeOfExpiration = input.timeOfExpiration?.let { Date(it) }
            )
            else -> PostModelImpl(
                    id = input.id,
                    allConnections = input.allConnections,
                    type = postType,
                    createdAt = Date(input.createdAt),
                    attachments = attachments,
                    locationPlace = input.location?.takeIf { it.en != null && it.ar != null }?.let {
                        converter.convert<LocationPlaceModel>(it)
                    },
                    originalCreatedAt = Date(input.originalCreatedAt),
                    originalId = input.originalId,
                    privacyConnections = input.privacyConnections?.toSet() ?: emptySet(),
                    privacyType = converter.convert(input.privacyType),
                    tags = input.tags ?: emptyList(),
                    text = input.text,
                    statusOfExpiration = convertExpiration(input.statusOfExpiration),
                    timeOfExpiration = input.timeOfExpiration?.let { Date(it) },
                    updatedAt = Date(input.updatedAt),
                    counters = converter.convert(input.counters),
                    author = convertAuthor(input.author, converter),
                    copyOwnerId = input.copyOwner,
                    price = input.price ?: 0.0,
                    autoSuggest = input.autoSuggest ?: PostAutoSuggest.EMPTY,
                    repostAuthor = input.repostAuthor?.run { convertAuthor(this, converter) }
            )
        }
    }

    private fun convertInfoPost(input: PostDbEntity, token: Any?, converter: ConvertersContext): InfoPostImpl {
        return convertPost(input, token, converter) as InfoPostImpl
    }

    private fun convertOfferPost(input: PostDbEntity, token: Any?, converter: ConvertersContext): OfferPostModelImpl {
        return convertPost(input, token, converter) as OfferPostModelImpl
    }

    private fun convertExpiration(expiration: String): ExpirationType {
        return when (expiration) {
            EXPIRATION_TYPE_ACTIVE -> ExpirationType.ACTIVE(expiration)
            EXPIRATION_TYPE_EXPIRED -> ExpirationType.EXPIRED(expiration)
            EXPIRATION_TYPE_CLOSED -> ExpirationType.CLOSED(expiration)
            EXPIRATION_TYPE_FULFILLED -> ExpirationType.FULFILLED(expiration)
            else -> throw  IllegalArgumentException("There is no convert type for $expiration")
        }
    }

    private fun convertAuthor(input: Map<String, ShortAccountDbEntity?>, converter: ConvertersContext): ShortAccountModel {
        val entity = requireNotNull(input.values.first())
        entity.id = input.keys.first()

        return converter.convert(entity, ShortAccountModel::class.java)
    }

    private fun convertPostType(input: String): PostType {
        return when (input) {
            NEWS_FEED_TYPE_NEED -> PostType.NEED()
            NEWS_FEED_TYPE_ACCOUNT -> PostType.PROFILE()
            NEWS_FEED_TYPE_OFFER -> PostType.OFFER()
            NEWS_FEED_TYPE_GENERAL -> PostType.GENERAL()
            NEWS_FEED_TYPE_INFO -> PostType.INFO()
            else -> {
                Timber.e(IllegalArgumentException("Wrong post item type $input"))
                PostType.OTHER()
            }
        }
    }

    private fun convertFromPostType(input: PostType): String {
        return when(input) {
            is PostType.NEED -> NEWS_FEED_TYPE_NEED
            is PostType.OFFER -> NEWS_FEED_TYPE_OFFER
            is PostType.GENERAL -> NEWS_FEED_TYPE_GENERAL
            is PostType.PROFILE -> NEWS_FEED_TYPE_ACCOUNT
            is PostType.INFO -> NEWS_FEED_TYPE_INFO
            is PostType.OTHER -> ""
            else -> ""
        }
    }

    private fun convertPostPrivacyType(input: String): PostPrivacyType {
        return when (input) {
            NEWS_FEED_PRIVACY_TYPE_PUBLIC -> PostPrivacyType.PUBLIC()
            NEWS_FEED_PRIVACY_TYPE_PRIVATE -> PostPrivacyType.PRIVATE()
            NEWS_FEED_PRIVACY_TYPE_WORLD -> PostPrivacyType.WORLD()

            else -> throw IllegalArgumentException("Wrong post privacy type $input")
        }
    }

    private fun convertItemType(input: String): EntityType {
        return when (input) {
            NetworkContract.EntityType.POST -> EntityType.POST()
            NetworkContract.EntityType.EVENT -> EntityType.EVENT()

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

    private fun convertOfferCategory(input: OfferCategoryDbModel): OfferCategoryModel {
        return OfferCategoryModel(
                id = input.id,
                name = TranslatedWordModelImpl(languageProvider, "", input.en, input.en, input.ar),
                parentId = input.parentId
        )
    }
}