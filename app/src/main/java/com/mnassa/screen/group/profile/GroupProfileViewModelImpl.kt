package com.mnassa.screen.group.profile

import android.os.Bundle
import com.mnassa.core.addons.asyncWorker
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.*
import com.mnassa.extensions.isAdmin
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
        private val tagInteractor: TagInteractor) : MnassaViewModelImpl(), GroupProfileViewModel {

    private var resetCounterJob: Job? = null

    override val groupChannel: BroadcastChannel<GroupModel> = ConflatedBroadcastChannel()
    override val closeScreenChannel: BroadcastChannel<Unit> = ArrayBroadcastChannel(1)
    override val tagsChannel: BroadcastChannel<List<TagModel>> = ConflatedBroadcastChannel()
    override val newsFeedChannel: BroadcastChannel<ListItemEvent<List<PostModel>>> = ConflatedBroadcastChannel()
    override val groupPermissionsChannel: BroadcastChannel<GroupPermissions> = ConflatedBroadcastChannel()

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

        handleException {
            newsFeedChannel.send(ListItemEvent.Added(postsInteractor.loadAllByGroupIdImmediately(groupId)))
            postsInteractor.loadAllByGroupId(groupId).bufferize(this).consumeTo(newsFeedChannel)
        }

        handleException {
            groupChannel.consumeEach {
                groupPermissionsChannel.send(if (it.isAdmin) GroupPermissions.ADMIN_PERMISSIONS else it.permissions)
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

    override fun removePost(post: PostModel) {
        handleException {
            withProgressSuspend {
                postsInteractor.removePost(postId = post.id)
            }
        }
    }

    override fun delete() {
        handleException {
            withProgressSuspend {
                groupsInteractor.deleteGroup(groupId)
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