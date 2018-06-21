package com.mnassa.screen.posts.general.create

import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.GeoPlaceModel
import com.mnassa.domain.model.RawPostModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.consume

/**
 * Created by Peter on 4/30/2018.
 */
class CreateGeneralPostViewModelImpl(private val postId: String?,
                                     private val postsInteractor: PostsInteractor,
                                     private val tagInteractor: TagInteractor,
                                     private val placeFinderInteractor: PlaceFinderInteractor,
                                     private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), CreateGeneralPostViewModel {

    override val closeScreenChannel: BroadcastChannel<Unit> = ArrayBroadcastChannel(1)

    override fun applyChanges(post: RawPostModel) {
        handleException {
            withProgressSuspend {
                if (postId == null) {
                    postsInteractor.createGeneralPost(post)
                } else {
                    postsInteractor.updateGeneralPost(post)
                }
            }
            closeScreenChannel.send(Unit)
        }
    }

    override suspend fun getUser(userId: String): ShortAccountModel? = handleExceptionsSuspend { userProfileInteractor.getAccountByIdChannel(userId).consume { receive() } }
    override suspend fun getTag(tagId: String): TagModel? = tagInteractor.get(tagId)
    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> = placeFinderInteractor.getReqieredPlaces(constraint)
    override suspend fun canPromotePost(): Boolean = userProfileInteractor.getPermissions().consume { receive() }.canPromoteGeneralPost
    override suspend fun getPromotePostPrice(): Long = postsInteractor.getPromotePostPrice()
}