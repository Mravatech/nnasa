package com.mnassa.screen.group.profile.events

import android.os.Bundle
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl
import org.kodein.di.generic.instance

/**
 * Created by Peter on 09.08.2018.
 */
class GroupEventsController(args: Bundle) : MnassaControllerImpl<GroupEventsViewModel>() {
    override val layoutId: Int = R.layout.controller_group_profile_events
    private val groupId: String by lazy { args.getString(EXTRA_GROUP_ID) }
    override val viewModel: GroupEventsViewModel by instance(arg = groupId)


    companion object {
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"

        fun newInstance(groupId: String): GroupEventsController {
            val args = Bundle()
            args.putString(EXTRA_GROUP_ID, groupId)
            return GroupEventsController(args)
        }
    }
}