package com.mnassa.screen.posts.offer.create

import android.os.Bundle
import com.mnassa.core.addons.asyncWorker
import com.mnassa.domain.interactor.*
import com.mnassa.domain.model.*
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Created by Peter on 5/3/2018.
 */
class CreateOfferViewModelImpl(private val offerId: String?,
                               private val postsInteractor: PostsInteractor,
                               private val tagInteractor: TagInteractor,
                               private val placeFinderInteractor: PlaceFinderInteractor,
                               private val userInteractor: UserProfileInteractor,
                               private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), CreateOfferViewModel {

    override val closeScreenChannel: BroadcastChannel<Unit> = BroadcastChannel(1)
    private val categoryToSubCategory = HashMap<String, MutableList<OfferCategoryModel>>()
    private lateinit var offerCategoriesPromise: Deferred<List<OfferCategoryModel>>
    private val applyChangesMutex = Mutex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        offerCategoriesPromise = GlobalScope.asyncWorker {
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

    override suspend fun applyChanges(post: RawPostModel) {
        applyChangesMutex.withLock {
            handleExceptionsSuspend {
                withProgressSuspend {
                    if (offerId == null) {
                        postsInteractor.createOffer(post)
                    } else postsInteractor.updateOffer(post)
                }
                closeScreenChannel.send(Unit)
            }
        }
    }

    override suspend fun getUser(userId: String): ShortAccountModel? = handleExceptionsSuspend { userInteractor.getAccountByIdChannel(userId).consume { receive() } }
    override suspend fun getTag(tagId: String): TagModel? = handleExceptionsSuspend { tagInteractor.get(tagId) }
    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> = placeFinderInteractor.getReqieredPlaces(constraint)
    override suspend fun canPromotePost(): Boolean = handleExceptionsSuspend { userInteractor.getPermissions().consume { receive() }.canPromoteOfferPost } ?: false
    override suspend fun getPromotePostPrice(): Long = handleExceptionsSuspend { postsInteractor.getPromotePostPrice() } ?: 0L
    override suspend fun getConnectionsCount(): Long = handleExceptionsSuspend { connectionsInteractor.getConnectedConnections().receiveOrNull()?.size?.toLong() } ?: 0L

    override suspend fun getUserLocation(): LocationPlaceModel? = handleExceptionsSuspend { userInteractor.getProfileById(userInteractor.getAccountIdOrException())?.location }
}