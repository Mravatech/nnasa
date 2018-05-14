package com.mnassa.screen.group.members

import android.os.Bundle
import com.mnassa.R
import com.mnassa.domain.model.GroupModel
import com.mnassa.screen.base.MnassaControllerImpl
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/14/2018.
 */
class GroupMembersController(args: Bundle) : MnassaControllerImpl<GroupMembersViewModel>(args) {
    override val layoutId: Int = R.layout.controller_group_members
    override val viewModel: GroupMembersViewModel by instance()

    companion object {
        private const val EXTRA_GROUP = "EXTRA_GROUP"
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"

        fun newInstance(group: GroupModel): GroupMembersController {
            val args = Bundle()
            args.putString(EXTRA_GROUP_ID, group.id)
            args.putSerializable(EXTRA_GROUP, group)

            return GroupMembersController(args)
        }
    }
}