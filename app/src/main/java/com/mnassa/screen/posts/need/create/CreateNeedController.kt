package com.mnassa.screen.posts.need.create

import android.os.Bundle
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.domain.model.Post
import com.mnassa.screen.base.MnassaControllerImpl

/**
 * Created by Peter on 3/19/2018.
 */
class CreateNeedController(args: Bundle) : MnassaControllerImpl<CreateNeedViewModel>(args) {
    override val layoutId: Int = R.layout.controller_need_create
    override val viewModel: CreateNeedViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {


        }

    }

    companion object {
        private const val EXTRA_POST_TO_EDIT = "EXTRA_POST_TO_EDIT"

        fun newInstance() = CreateNeedController(Bundle())
        fun newInstanceEditMode(post: Post): CreateNeedController {
            val args = Bundle()
            args.putSerializable(EXTRA_POST_TO_EDIT, post)
            return CreateNeedController(args)
        }
    }
}