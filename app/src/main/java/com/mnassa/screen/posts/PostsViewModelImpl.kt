package com.mnassa.screen.posts

import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PermissionsModel
import com.mnassa.domain.model.PostModel
import com.mnassa.extensions.ReConsumeWhenAccountChangedArrayBroadcastChannel
import com.mnassa.extensions.ReConsumeWhenAccountChangedConflatedBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
class PostsViewModelImpl(private val postsInteractor: PostsInteractor,
                         private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), PostsViewModel {

    override val newsFeedChannel: BroadcastChannel<ListItemEvent<PostModel>> by ReConsumeWhenAccountChangedArrayBroadcastChannel(
            beforeReConsume = { it.send(ListItemEvent.Cleared()) },
            receiveChannelProvider = { postsInteractor.loadAll() })

    override val permissionsChannel: ConflatedBroadcastChannel<PermissionsModel> by ReConsumeWhenAccountChangedConflatedBroadcastChannel {
        userProfileInteractor.getPermissions()
    }

    override fun onAttachedToWindow(post: PostModel) {
        handleException { postsInteractor.onItemViewed(post) }
    }
}