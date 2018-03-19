package com.mnassa.screen.posts.need.details

import android.os.Bundle
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl

/**
 * Created by Peter on 3/19/2018.
 */
class NeedDetailsController(args: Bundle) : MnassaControllerImpl<NeedDetailsViewModel>(args) {
    override val layoutId: Int = R.layout.controller_need_details
    override val viewModel: NeedDetailsViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)


    }

    companion object {
        private const val EXTRA_NEED_ID = "EXTRA_NEED_ID"

        fun newInstance(needId: String): NeedDetailsController {
            val args = Bundle()
            args.putString(EXTRA_NEED_ID, needId)
            return NeedDetailsController(args)
        }
    }
}