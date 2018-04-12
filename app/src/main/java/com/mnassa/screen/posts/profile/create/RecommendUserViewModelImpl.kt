package com.mnassa.screen.posts.profile.create

import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.repository.UserRepository
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel

/**
 * Created by Peter on 4/12/2018.
 */
class RecommendUserViewModelImpl(
        private val postId: String?,
        private val postsInteractor: PostsInteractor,
        private val userRepository: UserRepository
) : MnassaViewModelImpl(), RecommendUserViewModel {
    override val closeScreenChannel: ArrayBroadcastChannel<Unit> = ArrayBroadcastChannel(1)

    override suspend fun getUser(userId: String): ShortAccountModel? = handleExceptionsSuspend { userRepository.getAccountById(userId) }

    override fun createPost(recommendedUser: ShortAccountModel, text: String, postPrivacyOptions: PostPrivacyOptions) {
        handleException {
            withProgressSuspend {
                if (postId == null) {
                    postsInteractor.createUserRecommendation(recommendedUser.id, text, postPrivacyOptions)
                } else {
                    postsInteractor.updateUserRecommendation(postId, recommendedUser.id, text)
                }

                closeScreenChannel.send(Unit)
            }
        }
    }
}