package com.mnassa.screen.posts.profile.create

import com.mnassa.domain.model.RawRecommendPostModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 4/12/2018.
 */
interface RecommendUserViewModel : MnassaViewModel {
    val closeScreenChannel: BroadcastChannel<Unit>
    suspend fun getUser(userId: String): ShortAccountModel?

    suspend fun applyChanges(post: RawRecommendPostModel)

    suspend fun canPromotePost(): Boolean

    suspend fun getPromotePostPrice(): Long
}