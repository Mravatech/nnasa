package com.mnassa.screen.posts.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.mnassa.R
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.*
import kotlinx.android.synthetic.main.item_news_feed_need.view.*

/**
 * Created by Peter on 3/14/2018.
 */
class NeedViewHolder(itemView: View, onClickListener: View.OnClickListener) : BasePostViewHolder(itemView, onClickListener) {

    override fun bind(item: PostModel) {
        with(itemView) {
            ivAvatar.avatarRound(item.author.avatar)
            tvUserName.text = item.author.formattedName
            tvTime.text = item.originalCreatedAt.toTimeAgo()
            tvDescription.text = item.formattedText
            tvDescription.movementMethod = null
            tvDescription.goneIfEmpty()

            tvViewsCount.text = item.counters.views.toString()
            tvCommentsCount.text = item.counters.comments.toString()
            tvOffersCount.text = item.counters.offers.toString()
            tvRecomendationsCount.text = item.counters.recommend.toString()
            tvRepostCount.text = item.counters.reposts.toString()

            rlClickableRoot.setOnClickListener(onClickListener)
            rlClickableRoot.tag = this@NeedViewHolder

            tvRepostCount.setOnClickListener(onClickListener)
            tvRepostCount.tag = this@NeedViewHolder

            rlAuthorRoot.setOnClickListener(onClickListener)
            rlAuthorRoot.tag = this@NeedViewHolder

            btnMoreOptions.setOnClickListener(onClickListener)
            btnMoreOptions.tag = this@NeedViewHolder

            bindImages(item)
            bindRepost(item)
            bindGroup(item)
        }
    }

    companion object {
        fun newInstance(
                parent: ViewGroup,
                onClickListener: View.OnClickListener,
                imagesCount: Int,
                isRepost: Boolean,
                isPromoted: Boolean,
                fromGroup: Boolean,
                hasOptions: Boolean
        ): NeedViewHolder {

            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_news_feed_need, parent, false)
            view.rlRepostRoot.isGone = !isRepost
            view.flImagesRoot.isGone = imagesCount == 0
            view.llPromotedRoot.isGone = !isPromoted
            view.rlGroupRoot.isGone = !fromGroup
            view.btnMoreOptions.isGone = !hasOptions

            val layoutParams = view.tvTime.layoutParams as RelativeLayout.LayoutParams
            if (view.btnMoreOptions.isGone) {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)
            } else {
                layoutParams.addRule(RelativeLayout.START_OF, R.id.btnMoreOptions)
            }
            view.tvTime.layoutParams = layoutParams

            if (imagesCount > 0) {
                val imagesLayout = when (imagesCount) {
                    1 -> R.layout.item_image_one
                    2 -> R.layout.item_image_two
                    3 -> R.layout.item_image_three
                    else -> R.layout.item_image_more
                }
                inflater.inflate(imagesLayout, view.flImagesRoot, true)
            }

            return NeedViewHolder(view, onClickListener)
        }
    }
}