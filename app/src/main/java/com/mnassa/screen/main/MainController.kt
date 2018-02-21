package com.mnassa.screen.main

import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl

/**
 * Created by Peter on 2/21/2018.
 */
class MainController : MnassaControllerImpl<MainViewModel>() {
    override val layoutId: Int = R.layout.controller_main
    override val viewModel: MainViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
    }

    companion object {
        fun newInstance() = MainController()
    }
}