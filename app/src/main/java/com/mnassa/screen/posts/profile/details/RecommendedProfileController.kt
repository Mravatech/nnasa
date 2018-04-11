package com.mnassa.screen.posts.profile.details

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.domain.model.RecommendedProfilePostModel
import com.mnassa.screen.base.MnassaControllerImpl
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/10/2018.
 */
class RecommendedProfileController(args: Bundle) : MnassaControllerImpl<RecommendedProfileViewModel>(args) {
    override val layoutId: Int = R.layout.controller_recommended_profile
    override val viewModel: RecommendedProfileViewModel by instance()

    override fun onViewCreated(view: View) {

    }

    companion object {
        private const val EXTRA_POST = "EXTRA_POST"

        fun newInstance(post: RecommendedProfilePostModel): RecommendedProfileController {
            val args = Bundle()
            args.putSerializable(EXTRA_POST, post)
            return RecommendedProfileController(args)
        }
    }
}