package com.mnassa.screen.group.permissions

import android.os.Bundle
import com.mnassa.R
import com.mnassa.domain.model.GroupModel
import com.mnassa.screen.base.MnassaControllerImpl
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/30/2018.
 */
class GroupPermissionsController(args: Bundle) : MnassaControllerImpl<GroupPermissionsViewModel>(args) {
    override val layoutId: Int = R.layout.controller_group_permissions
    private val groupId: String = args.getString(EXTRA_GROUP_ID)
    private var group: GroupModel = args.getSerializable(EXTRA_GROUP) as GroupModel
    override val viewModel: GroupPermissionsViewModel by instance()

    companion object {
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"
        private const val EXTRA_GROUP = "EXTRA_GROUP"

        fun newInstance(group: GroupModel): GroupPermissionsController {
            val args = Bundle()
            args.putString(EXTRA_GROUP_ID, group.id)
            args.putSerializable(EXTRA_GROUP, group)
            return GroupPermissionsController(args)
        }
    }


}