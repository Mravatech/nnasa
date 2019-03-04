package com.mnassa.screen.group.members

import android.os.Bundle
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach

/**
 * Created by Peter on 5/14/2018.
 */
class GroupMembersViewModelImpl(
        private val groupId: String,
        private val groupsInteractor: GroupsInteractor) : MnassaViewModelImpl(), GroupMembersViewModel {

    override val groupMembersChannel: BroadcastChannel<List<GroupMemberItem>> = ConflatedBroadcastChannel()
    override val groupChannel: BroadcastChannel<GroupModel> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resolveExceptions {
            groupsInteractor.getGroupMembers(groupId).consumeEach { members ->
                val group = groupChannel.consume { receive() }
                groupMembersChannel.send(members.map { GroupMemberItem(it, group.admins.contains(it.id)) })
            }
        }
        resolveExceptions {
            groupsInteractor.getGroup(groupId).consumeEach { group ->
                if (group != null) {
                    groupChannel.send(group)
                    groupMembersChannel.consume { receiveOrNull() }?.let { members ->
                        groupMembersChannel.send(members.map { it.copy(isAdmin = group.admins.contains(it.user.id)) })
                    }
                }
            }
        }
    }

    override fun removeMember(member: ShortAccountModel) {
        resolveExceptions {
            withProgressSuspend {
                groupsInteractor.removeMember(groupId, member.id)
            }
        }
    }

    override fun makeAdmin(member: ShortAccountModel) {
        resolveExceptions {
            withProgressSuspend {
                groupsInteractor.makeAdmin(groupId, member.id)
            }
        }
    }

    override fun unMakeAdmin(member: ShortAccountModel) {
        resolveExceptions {
            withProgressSuspend {
                groupsInteractor.unMakeAdmin(groupId, member.id)
            }
        }
    }
}