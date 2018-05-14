package com.mnassa.screen.posts.profile.create

import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 4/12/2018.
 */
interface RecommendUserViewModel : MnassaViewModel {
    val closeScreenChannel: BroadcastChannel<Unit>
    suspend fun getUser(userId: String): ShortAccountModel?

    fun createPost(recommendedUser: ShortAccountModel, text: String, postPrivacyOptions: PostPrivacyOptions)

    suspend fun canPromotePost(): Boolean

    suspend fun getPromotePostPrice(): Long
}