package com.mnassa.screen.posts.need.create

import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Created by Peter on 3/19/2018.
 */
class CreateNeedViewModelImpl(
        private val postId: String?,
        private val postsInteractor: PostsInteractor,
        private val tagInteractor: TagInteractor,
        private val placeFinderInteractor: PlaceFinderInteractor,
        private val userInteractor: UserProfileInteractor
) : MnassaViewModelImpl(), CreateNeedViewModel {

    override val closeScreenChannel: BroadcastChannel<Unit> = BroadcastChannel(1)
    private val applyChangesMutex = Mutex()

    override suspend fun applyChanges(post: RawPostModel) {
        applyChangesMutex.withLock {
            handleExceptionsSuspend {
                withProgressSuspend {
                    if (postId == null) {
                        postsInteractor.createNeed(post)
                    } else {
                        postsInteractor.updateNeed(post)
                    }
                    closeScreenChannel.send(Unit)
                }
            }
        }
    }

    override suspend fun getDefaultExpirationDays(): Long = postsInteractor.getDefaultExpirationDays()

    override suspend fun getUser(userId: String): ShortAccountModel? = handleExceptionsSuspend { userInteractor.getAccountByIdChannel(userId).consume { receive() } }
    override suspend fun getTag(tagId: String): TagModel? = handleExceptionsSuspend { tagInteractor.get(tagId) }
    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> = placeFinderInteractor.getReqieredPlaces(constraint)
    override suspend fun canPromotePost(): Boolean = handleExceptionsSuspend { userInteractor.getPermissions().consume { receive() }.canPromoteNeedPost } ?: false
    override suspend fun getPromotePostPrice(): Long = handleExceptionsSuspend { postsInteractor.getPromotePostPrice() } ?: 0L
    override suspend fun getUserLocation(): LocationPlaceModel? = handleExceptionsSuspend { userInteractor.getProfileById(userInteractor.getAccountIdOrException())?.location }
}