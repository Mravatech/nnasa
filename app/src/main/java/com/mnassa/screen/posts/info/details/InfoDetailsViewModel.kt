package com.mnassa.screen.posts.info.details

import com.mnassa.domain.model.PostModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 4/30/2018.
 */
interface InfoDetailsViewModel : MnassaViewModel {
    val closeScreenChannel: BroadcastChannel<Unit>

    fun hidePost(post: PostModel)
}