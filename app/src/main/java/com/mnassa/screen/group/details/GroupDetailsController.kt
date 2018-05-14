package com.mnassa.screen.group.details

import android.os.Bundle
import com.mnassa.R
import com.mnassa.domain.model.GroupModel
import com.mnassa.screen.base.MnassaControllerImpl
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/14/2018.
 */
class GroupDetailsController(args: Bundle) : MnassaControllerImpl<GroupDetailsViewModel>(args) {
    override val layoutId: Int = R.layout.controller_group_details
    override val viewModel: GroupDetailsViewModel by instance()

    companion object {
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"
        private const val EXTRA_GROUP = "EXTRA_GROUP"

        fun newInstance(group: GroupModel): GroupDetailsController {
            val args = Bundle()
            args.putString(EXTRA_GROUP_ID, group.id)
            args.putSerializable(EXTRA_GROUP, group)

            return GroupDetailsController(args)
        }
    }
}