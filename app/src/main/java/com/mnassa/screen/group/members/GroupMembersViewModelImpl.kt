package com.mnassa.screen.group.members

import android.os.Bundle
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 5/14/2018.
 */
class GroupMembersViewModelImpl(
        private val groupId: String,
        private val groupsInteractor: GroupsInteractor) : MnassaViewModelImpl(), GroupMembersViewModel {

    override val groupMembersChannel: BroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()
    override val groupChannel: BroadcastChannel<GroupModel> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            groupsInteractor.getGroupMembers(groupId).consumeTo(groupMembersChannel)
        }

        handleException {
            groupsInteractor.getGroup(groupId).consumeEach {
                if (it != null) {
                    groupChannel.send(it)
                }
            }
        }
    }

    override fun removeMember(member: ShortAccountModel) {
        handleException {
            withProgressSuspend {
                groupsInteractor.removeMember(groupId, member.id)
            }
        }
    }

    override fun makeAdmin(member: ShortAccountModel) {
        handleException {
            withProgressSuspend {
                groupsInteractor.makeAdmin(groupId, member.id)
            }
        }
    }

    override fun unMakeAdmin(member: ShortAccountModel) {
        handleException {
            withProgressSuspend {
                groupsInteractor.unMakeAdmin(groupId, member.id)
            }
        }
    }
}