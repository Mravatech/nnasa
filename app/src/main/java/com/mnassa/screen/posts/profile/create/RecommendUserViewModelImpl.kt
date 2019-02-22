package com.mnassa.screen.posts.profile.create

import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.RawRecommendPostModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Created by Peter on 4/12/2018.
 */
class RecommendUserViewModelImpl(
        private val postId: String?,
        private val postsInteractor: PostsInteractor,
        private val userInteractor: UserProfileInteractor
) : MnassaViewModelImpl(), RecommendUserViewModel {

    override val closeScreenChannel: BroadcastChannel<Unit> = BroadcastChannel(1)
    private val applyChangesMutex = Mutex()

    override suspend fun getUser(userId: String): ShortAccountModel? = handleExceptionsSuspend { userInteractor.getAccountByIdChannel(userId).consume { receive() } }

    override suspend fun applyChanges(post: RawRecommendPostModel) {
        applyChangesMutex.withLock {
            handleExceptionsSuspend {
                withProgressSuspend {
                    if (postId == null) {
                        postsInteractor.createUserRecommendation(post)
                    } else {
                        postsInteractor.updateUserRecommendation(post)
                    }
                    closeScreenChannel.send(Unit)
                }
            }
        }
    }

    override suspend fun canPromotePost(): Boolean = userInteractor.getPermissions().consume { receive() }.canPromoteAccountPost
    override suspend fun getPromotePostPrice(): Long = postsInteractor.getPromotePostPrice()
}