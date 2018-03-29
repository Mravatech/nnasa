package com.mnassa.screen.posts.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mnassa.R
import com.mnassa.domain.model.Post
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.formattedText
import com.mnassa.extensions.image
import com.mnassa.extensions.toTimeAgo
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_news_feed_need.view.*

/**
 * Created by Peter on 3/14/2018.
 */
class NeedViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<Post>(itemView) {

    override fun bind(item: Post) {
        with(itemView) {
            ivAvatar.avatarRound(item.author.avatar)
            tvUserName.text = item.author.formattedName
            tvTime.text = item.createdAt.toTimeAgo()
            tvDescription.text = item.formattedText

            tvViewsCount.text = item.counters.views.toString()
            tvCommentsCount.text = item.counters.comments.toString()

            rlClickableRoot.setOnClickListener(onClickListener)
            rlClickableRoot.tag = this@NeedViewHolder

            btnAction.visibility = if (item.autoSuggest.youCanHelp || item.autoSuggest.aids.isNotEmpty()) View.VISIBLE else View.GONE
            btnAction.text = when {
                item.autoSuggest.youCanHelp && item.autoSuggest.aids.isNotEmpty() ->
                    fromDictionary(R.string.need_item_btn_you_and_connections_can_help).format(item.autoSuggest.aids.size)
                item.autoSuggest.youCanHelp -> fromDictionary(R.string.need_item_btn_you_can_help)
                item.autoSuggest.aids.isNotEmpty() -> fromDictionary(R.string.need_item_btn_connections_can_help).format(item.autoSuggest.aids.size)
                else -> null
            }

            btnAction.setOnClickListener(onClickListener)
            btnAction.tag = this@NeedViewHolder
            setImages(item)
        }
    }

    private fun setImages(item: Post) {
        with(itemView) {
            if (item.images.isNotEmpty()) {
                itemView.findViewById<ImageView>(R.id.ivOne).image(item.images[0])
            }

            if (item.images.size >= 2) {
                itemView.findViewById<ImageView>(R.id.ivTwo).image(item.images[1])
            }

            if (item.images.size >= 3) {
                itemView.findViewById<ImageView>(R.id.ivThree).image(item.images[2])
            }

            if (item.images.size > 3) {
                itemView.findViewById<TextView>(R.id.tvCountMore).text = "+${item.images.size - 2}"
            }
        }
    }

    companion object {
        fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): NeedViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news_feed_need, parent, false)
            view.flImagesRoot.visibility = View.GONE
            return NeedViewHolder(view, onClickListener)
        }

        fun newInstanceWithImage(parent: ViewGroup, onClickListener: View.OnClickListener, imagesCount: Int): NeedViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_news_feed_need, parent, false)
            view.flImagesRoot.visibility = View.VISIBLE
            val imagesLayout = when (imagesCount) {
                1 -> R.layout.item_image_one
                2 -> R.layout.item_image_two
                3 -> R.layout.item_image_three
                else -> R.layout.item_image_more
            }
            inflater.inflate(imagesLayout, view.flImagesRoot, true)
            return NeedViewHolder(view, onClickListener)
        }
    }
}