package com.mnassa.screen.posts.offer.create

import android.os.Bundle
import com.mnassa.domain.interactor.*
import com.mnassa.domain.model.*
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.consume

/**
 * Created by Peter on 5/3/2018.
 */
class CreateOfferViewModelImpl(private val offerId: String?,
                               private val postsInteractor: PostsInteractor,
                               private val tagInteractor: TagInteractor,
                               private val placeFinderInteractor: PlaceFinderInteractor,
                               private val userRepository: UserProfileInteractor,
                               private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), CreateOfferViewModel {

    override val closeScreenChannel: BroadcastChannel<Unit> = ArrayBroadcastChannel(1)
    private val categoryToSubCategory = HashMap<String, MutableList<OfferCategoryModel>>()
    private lateinit var offerCategoriesPromise: Deferred<List<OfferCategoryModel>>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        offerCategoriesPromise = async {
            handleExceptionsSuspend<List<OfferCategoryModel>> {
                val categories = mutableListOf<OfferCategoryModel>()
                postsInteractor.loadOfferCategories().forEach {
                    when {
                        it.isCategory -> {
                            categories += it
                        }
                        it.isSubCategory -> {
                            categoryToSubCategory.getOrPut(requireNotNull(it.parentId), { mutableListOf() }) += it
                        }
                    }
                }

                categories

            } ?: emptyList()
        }
    }

    override suspend fun getOfferCategories(): List<OfferCategoryModel> = offerCategoriesPromise.await()

    override suspend fun getOfferSubCategories(category: OfferCategoryModel): List<OfferCategoryModel> {
        offerCategoriesPromise.await()
        return categoryToSubCategory[category.id] ?: emptyList()
    }

    override suspend fun getShareOfferPostPrice(): Long? = postsInteractor.getShareOfferPostPrice()

    override suspend fun getShareOfferPostPerUserPrice(): Long? = postsInteractor.getShareOfferPostPerUserPrice()

    override fun applyChanges(post: RawPostModel) {
        handleException {
            withProgressSuspend {
                if (offerId == null) {
                    postsInteractor.createOffer(post)
                } else postsInteractor.updateOffer(post)
            }
            closeScreenChannel.send(Unit)
        }
    }

    override suspend fun getUser(userId: String): ShortAccountModel? = handleExceptionsSuspend { userRepository.getAccountByIdChannel(userId).consume { receive() } }
    override suspend fun getTag(tagId: String): TagModel? = tagInteractor.get(tagId)
    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> = placeFinderInteractor.getReqieredPlaces(constraint)
    override suspend fun canPromotePost(): Boolean = userRepository.getPermissions().consume { receive() }.canPromoteOfferPost
    override suspend fun getPromotePostPrice(): Long = postsInteractor.getPromotePostPrice()
    override suspend fun getConnectionsCount(): Long = handleExceptionsSuspend { connectionsInteractor.getConnectedConnections().consume { receive() }.size.toLong() } ?: 0L
}