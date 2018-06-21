package com.mnassa.screen.posts.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.InfoPostModel
import com.mnassa.domain.model.PostModel
import com.mnassa.extensions.formattedText
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.extensions.isGone
import com.mnassa.extensions.toTimeAgo
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_news_feed_info.view.*

/**
 * Created by Peter on 4/30/2018.
 */
class InfoViewHolder(itemView: View, onClickListener: View.OnClickListener) : BasePostViewHolder(itemView, onClickListener) {

    override fun bind(item: PostModel) {
        item as InfoPostModel

        with(itemView) {
            tvUserName.text = fromDictionary(R.string.general_author)
            tvTime.text = item.originalCreatedAt.toTimeAgo()
            tvTitle.text = item.title
            tvDescription.text = item.formattedText
            tvDescription.goneIfEmpty()

            rlClickableRoot.setOnClickListener(onClickListener)
            rlClickableRoot.tag = this@InfoViewHolder

            btnHidePost.text = fromDictionary(R.string.general_thanks_got_it)

            btnHidePost.setOnClickListener(onClickListener)
            btnHidePost.tag = this@InfoViewHolder

            rlAuthorRoot.setOnClickListener(onClickListener)
            rlAuthorRoot.tag = this@InfoViewHolder

            bindImages(item)
        }
    }

    companion object {
        fun newInstance(
                parent: ViewGroup,
                onClickListener: View.OnClickListener,
                imagesCount: Int = 0,
                isPinned: Boolean = false
        ): InfoViewHolder {

            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_news_feed_info, parent, false)
            view.btnHidePost.isGone = !isPinned
            view.vBtnHidePostDivider.isGone = !isPinned
            view.flImagesRoot.isGone = imagesCount == 0

            if (imagesCount > 0) {
                val imagesLayout = when (imagesCount) {
                    1 -> R.layout.item_image_one
                    2 -> R.layout.item_image_two
                    3 -> R.layout.item_image_three
                    else -> R.layout.item_image_more
                }
                inflater.inflate(imagesLayout, view.flImagesRoot, true)
            }
            return InfoViewHolder(view, onClickListener)
        }
    }
}