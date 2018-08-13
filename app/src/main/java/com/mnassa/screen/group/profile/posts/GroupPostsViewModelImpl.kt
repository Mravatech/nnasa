package com.mnassa.screen.group.profile.posts

import android.os.Bundle
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.withBuffer
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.produce

/**
 * Created by Peter on 09.08.2018.
 */
class GroupPostsViewModelImpl(private val groupId: String,
                              private val postsInteractor: PostsInteractor,
                              private val groupsInteractor: GroupsInteractor) : MnassaViewModelImpl(), GroupPostsViewModel {

    override val groupChannel: ConflatedBroadcastChannel<GroupModel> = ConflatedBroadcastChannel()
    override val newsFeedChannel: ReceiveChannel<ListItemEvent<List<PostModel>>>
        get() = produce {
            send(ListItemEvent.Added(postsInteractor.loadAllByGroupIdImmediately(groupId)))
            postsInteractor.loadAllByGroupId(groupId).withBuffer().consumeEach { send(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleException {
            groupsInteractor.getGroup(groupId).consumeEach {
                if (it != null) groupChannel.send(it)
            }
        }
    }

    override fun removePost(post: PostModel) {
        handleException {
            withProgressSuspend {
                postsInteractor.removePost(postId = post.id)
            }
        }
    }

    override fun onAttachedToWindow(post: PostModel) {
        handleException { postsInteractor.onItemViewed(post) }
    }

    override fun hideInfoPost(post: PostModel) {
        handleException {
            withProgressSuspend {
                postsInteractor.hideInfoPost(post.id)
            }
        }
    }
}