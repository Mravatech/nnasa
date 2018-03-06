package com.mnassa.screen.needs

import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl

/**
 * Created by Peter on 3/6/2018.
 */
class NeedsController : MnassaControllerImpl<NeedsViewModel>() {
    override val layoutId: Int = R.layout.controller_needs_list
    override val viewModel: NeedsViewModel by instance()

    companion object {
        fun newInstance() = NeedsController()
    }
}