package com.mnassa.screen.group.create

import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/22/2018.
 */
class CreateGroupController : MnassaControllerImpl<CreateGroupViewModel>() {
    override val layoutId: Int = R.layout.controller_group_create
    override val viewModel: CreateGroupViewModel by instance()

    companion object {
        fun newInstance() = CreateGroupController()
    }
}