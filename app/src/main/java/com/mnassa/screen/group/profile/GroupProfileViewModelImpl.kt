package com.mnassa.screen.group.profile

import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.GroupPermissions
import com.mnassa.domain.model.TagModel
import com.mnassa.extensions.isAdmin
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach

/**
 * Created by Peter on 5/14/2018.
 */
class GroupProfileViewModelImpl(
        private val groupId: String,
        private val groupsInteractor: GroupsInteractor,
        private val userProfileInteractor: UserProfileInteractor,
        private val tagInteractor: TagInteractor) : MnassaViewModelImpl(), GroupProfileViewModel {

    override val groupChannel: BroadcastChannel<GroupModel> = ConflatedBroadcastChannel()
    override val closeScreenChannel: BroadcastChannel<Unit> = BroadcastChannel(1)
    override val tagsChannel: BroadcastChannel<List<TagModel>> = ConflatedBroadcastChannel()
    override val groupPermissionsChannel: BroadcastChannel<GroupPermissions> = ConflatedBroadcastChannel()
    override val isMemberChannel: BroadcastChannel<Boolean> = ConflatedBroadcastChannel()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            groupsInteractor.getGroup(groupId).consumeEach {
                if (it != null) {
                    groupChannel.send(it)
                    tagsChannel.send(loadTags(it.tags))
                } else {
                    closeScreenChannel.send(Unit)
                }
            }
        }
        setupScope.launchWorker {
            groupChannel.consumeEach {
                groupPermissionsChannel.send(if (it.isAdmin) GroupPermissions.ADMIN_PERMISSIONS else it.permissions)
            }
        }
        setupScope.launchWorker {
            groupsInteractor.getGroupMembers(groupId).consumeEach { members ->
                val userId = userProfileInteractor.getAccountIdOrException()
                isMemberChannel.send(members.any { it.id == userId })
            }
        }
    }

    override fun leave() {
        launchWorker {
            withProgressSuspend {
                groupsInteractor.leaveGroup(groupId)
            }
            closeScreenChannel.send(Unit)
        }
    }

    override fun delete() {
        launchWorker {
            withProgressSuspend {
                groupsInteractor.deleteGroup(groupId)
            }
            closeScreenChannel.send(Unit)
        }
    }


    private suspend fun loadTags(tags: List<String>): List<TagModel> = handleExceptionsSuspend { tagInteractor.get(tags) } ?: emptyList()
}