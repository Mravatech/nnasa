package com.mnassa.screen.posts.info.details

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.InfoPostModel
import com.mnassa.domain.model.PostModel
import com.mnassa.extensions.isGone
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.details.adapter.PostAttachmentsAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_info_post_details.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/30/2018.
 */
class InfoDetailsController(args: Bundle) : MnassaControllerImpl<InfoDetailsViewModel>(args) {
    override val layoutId: Int = R.layout.controller_info_post_details
    override val viewModel: InfoDetailsViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        val post = args[EXTRA_POST] as InfoPostModel

        if (post.isPinned) {
            view.toolbar.withActionButton(fromDictionary(R.string.general_details_got_it)) {
                viewModel.hidePost(post)
            }
        }

        launchCoroutineUI {
            viewModel.closeScreenChannel.consumeEach { close() }
        }

        bindPost(post)
    }

    private fun bindPost(post: InfoPostModel) {
        with(view ?: return) {
            tvInfoPostTitle.text = post.title
            tvInfoPostDescription.text = post.text

            //attachments
            flImages.isGone = post.attachments.isEmpty()
            if (post.attachments.isNotEmpty()) {
                pivImages.count = post.attachments.size
                pivImages.selection = 0

                vpImages.adapter = PostAttachmentsAdapter(post.attachments)
                vpImages.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                    override fun onPageSelected(position: Int) {
                        pivImages.selection = position
                    }
                })
            }

            tvViewsCount.text = fromDictionary(R.string.need_views_count).format(post.counters.views)
        }
    }


    companion object {
        private const val EXTRA_POST = "EXTRA_POST"

        fun newInstance(post: PostModel): InfoDetailsController {
            val args = Bundle()
            args.putSerializable(EXTRA_POST, post)
            return InfoDetailsController(args)
        }

    }
}