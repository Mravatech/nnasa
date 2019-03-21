package com.mnassa.screen.group.profile.posts

import com.mnassa.core.addons.launchWorker
import com.mnassa.core.addons.launchWorkerNoExceptions
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.withBuffer
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce

/**
 * Created by Peter on 09.08.2018.
 */
class GroupPostsViewModelImpl(private val groupId: String,
                              private val postsInteractor: PostsInteractor,
                              private val groupsInteractor: GroupsInteractor) : MnassaViewModelImpl(), GroupPostsViewModel {

    override val groupChannel: ConflatedBroadcastChannel<GroupModel> = ConflatedBroadcastChannel()
    override val newsFeedChannel: ReceiveChannel<ListItemEvent<List<PostModel>>>
        get() = produce {
            handleExceptionsSuspend { send(ListItemEvent.Added(postsInteractor.loadAllByGroupIdImmediately(groupId))) }
            postsInteractor.loadAllByGroupId(groupId).withBuffer().consumeEach { send(it) }
        }

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            groupsInteractor.getGroup(groupId).consumeEach {
                if (it != null) groupChannel.send(it)
            }
        }
    }

    override fun removePost(post: PostModel) {
        launchWorker {
            withProgressSuspend {
                postsInteractor.removePost(postId = post.id)
            }
        }
    }

    override fun onAttachedToWindow(post: PostModel) {
        GlobalScope.launchWorkerNoExceptions {
            postsInteractor.onItemViewed(post)
        }
    }

    override fun hideInfoPost(post: PostModel) {
        launchWorker {
            withProgressSuspend {
                postsInteractor.hideInfoPost(post.id)
            }
        }
    }
}