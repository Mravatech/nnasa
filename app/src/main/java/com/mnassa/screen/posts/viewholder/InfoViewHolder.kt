package com.mnassa.screen.posts.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mnassa.R
import com.mnassa.domain.model.InfoPostModel
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.*
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_news_feed_info.view.*

/**
 * Created by Peter on 4/30/2018.
 */
class InfoViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<PostModel>(itemView) {

    override fun bind(item: PostModel) {
        item as InfoPostModel

        with(itemView) {
//            ivAvatar.avatarRound(item.author.avatar)
            tvUserName.text = fromDictionary(R.string.general_author)   //item.author.formattedName
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

    private fun bindImages(item: PostModel) {
        with(itemView) {
            if (item.attachments.isNotEmpty()) {
                itemView.findViewById<ImageView>(R.id.ivOne).image(item.attachments[0])
            }

            if (item.attachments.size >= 2) {
                itemView.findViewById<ImageView>(R.id.ivTwo).image(item.attachments[1])
            }

            if (item.attachments.size >= 3) {
                itemView.findViewById<ImageView>(R.id.ivThree).image(item.attachments[2])
            }

            if (item.attachments.size > 3) {
                itemView.findViewById<TextView>(R.id.tvCountMore).text = "+${item.attachments.size - 2}"
            }
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