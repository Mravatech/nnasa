package com.mnassa.screen.group.profile

import android.os.Bundle
import com.mnassa.core.addons.asyncWorker
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
import com.mnassa.extensions.ProcessAccountChangeArrayBroadcastChannel
import com.mnassa.extensions.ProcessAccountChangeConflatedBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.delay

/**
 * Created by Peter on 5/14/2018.
 */
class GroupProfileViewModelImpl(
        private val groupId: String,
        private val groupsInteractor: GroupsInteractor,
        private val postsInteractor: PostsInteractor,
        private val userProfileInteractor: UserProfileInteractor,
        private val tagInteractor: TagInteractor) : MnassaViewModelImpl(), GroupProfileViewModel {

    private var resetCounterJob: Job? = null

    override val groupChannel: BroadcastChannel<GroupModel> = ConflatedBroadcastChannel()
    override val closeScreenChannel: BroadcastChannel<Unit> = ArrayBroadcastChannel(1)
    override val tagsChannel: BroadcastChannel<List<TagModel>> = ConflatedBroadcastChannel()

    override val newsFeedChannel: BroadcastChannel<ListItemEvent<PostModel>>by ProcessAccountChangeArrayBroadcastChannel(
            beforeReConsume = { it.send(ListItemEvent.Cleared()) },
            receiveChannelProvider = { postsInteractor.loadAllByGroupId(groupId) })

    override val infoFeedChannel: BroadcastChannel<ListItemEvent<InfoPostModel>>by ProcessAccountChangeArrayBroadcastChannel(
            receiveChannelProvider = { postsInteractor.loadAllInfoPosts() })

    override val permissionsChannel: BroadcastChannel<PermissionsModel>by ProcessAccountChangeConflatedBroadcastChannel {
        userProfileInteractor.getPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            groupsInteractor.getGroup(groupId).consumeEach {
                if (it != null) {
                    groupChannel.send(it)
                    tagsChannel.send(loadTags(it.tags))
                } else {
                    closeScreenChannel.send(Unit)
                }
            }
        }
    }

    override fun onAttachedToWindow(post: PostModel) {
        handleException { postsInteractor.onItemViewed(post) }

        //reset counter with debounce
        resetCounterJob?.cancel()
        resetCounterJob = async {
            delay(1_000)
            resetCounter()
        }
    }

    override fun hideInfoPost(post: PostModel) {
        handleException {
            withProgressSuspend {
                postsInteractor.hideInfoPost(post.id)
            }
        }
    }

    override fun leave() {
        handleException {
            withProgressSuspend {
                groupsInteractor.leaveGroup(groupId)
            }
            closeScreenChannel.send(Unit)
        }
    }

    private fun resetCounter() {
        handleException {
            postsInteractor.resetCounter()
        }
    }

    private suspend fun loadTags(tags: List<String>): List<TagModel> {
        return tags.map { tag -> asyncWorker { tagInteractor.get(tag) } }.mapNotNull { it.await() }
    }
}