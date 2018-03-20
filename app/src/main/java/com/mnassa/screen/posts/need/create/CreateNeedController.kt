package com.mnassa.screen.posts.need.create

import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl

/**
 * Created by Peter on 3/19/2018.
 */
class CreateNeedController : MnassaControllerImpl<CreateNeedViewModel>() {
    override val layoutId: Int = R.layout.controller_need_create
    override val viewModel: CreateNeedViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with (view) {



        }

    }

    companion object {
        fun newInstance() = CreateNeedController()
    }
}