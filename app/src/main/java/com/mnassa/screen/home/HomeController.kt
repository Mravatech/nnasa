package com.mnassa.screen.home

import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl

/**
 * Created by Peter on 3/6/2018.
 */
class HomeController : MnassaControllerImpl<HomeViewModel>() {
    override val layoutId: Int = R.layout.controller_home
    override val viewModel: HomeViewModel by instance()

    companion object {
        fun newInstance() = HomeController()
    }
}