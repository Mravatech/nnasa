package com.mnassa.screen.posts.need.details

import com.mnassa.domain.model.Post
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/19/2018.
 */
interface NeedDetailsViewModel : MnassaViewModel {
    val postChannel: BroadcastChannel<Post>
    val postTagsChannel: BroadcastChannel<List<TagModel>>
    val finishScreenChannel: BroadcastChannel<Unit>
    fun delete()

}