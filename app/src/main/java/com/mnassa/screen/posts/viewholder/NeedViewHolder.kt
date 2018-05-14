package com.mnassa.screen.posts.viewholder

import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mnassa.R
import com.mnassa.domain.model.ExpirationType
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.*
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_news_feed_need.view.*
import kotlinx.android.synthetic.main.item_promoted_panel.view.*

/**
 * Created by Peter on 3/14/2018.
 */
class NeedViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<PostModel>(itemView) {

    override fun bind(item: PostModel) {
        with(itemView) {
            ivAvatar.avatarRound(item.author.avatar)
            tvUserName.text = item.author.formattedName
            tvTime.text = item.originalCreatedAt.toTimeAgo()
            tvDescription.text = item.formattedText
            tvDescription.goneIfEmpty()

            tvViewsCount.text = item.counters.views.toString()
            tvCommentsCount.text = item.counters.comments.toString()

            rlClickableRoot.setOnClickListener(onClickListener)
            rlClickableRoot.tag = this@NeedViewHolder

            if (item.statusOfExpiration is ExpirationType.ACTIVE) {
                tvAction.visibility = if (item.autoSuggest.youCanHelp || item.autoSuggest.accountIds.isNotEmpty()) View.VISIBLE else View.GONE
                tvAction.text = when {
                    item.autoSuggest.youCanHelp && item.autoSuggest.accountIds.isNotEmpty() ->
                        fromDictionary(R.string.need_item_btn_you_and_connections_can_help).format(item.autoSuggest.accountIds.size)
                    item.autoSuggest.youCanHelp -> fromDictionary(R.string.need_item_btn_you_can_help)
                    item.autoSuggest.accountIds.isNotEmpty() -> fromDictionary(R.string.need_item_btn_connections_can_help).format(item.autoSuggest.accountIds.size)
                    else -> null
                }
                tvAction.setTextColor(ContextCompat.getColor(context, R.color.accent))
            } else {
                tvAction.visibility = View.VISIBLE
                tvAction.bindExpireType(item.statusOfExpiration, item.timeOfExpiration)
            }

            tvAction.setOnClickListener(onClickListener)
            tvAction.tag = this@NeedViewHolder

            rlAuthorRoot.setOnClickListener(onClickListener)
            rlAuthorRoot.tag = this@NeedViewHolder

            bindImages(item)
            bindRepost(item)
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

    private fun bindRepost(item: PostModel) {
        if (!item.isRepost) return

        with(itemView) {
            val repostedBySpan = SpannableStringBuilder(fromDictionary(R.string.need_item_reposted_by))
            repostedBySpan.append(" ")
            val startSpan = repostedBySpan.length
            repostedBySpan.append(requireNotNull(item.repostAuthor).formattedName)
            repostedBySpan.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)),
                    startSpan,
                    repostedBySpan.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            tvReplyText.text = repostedBySpan
            tvReplyTime.text = item.createdAt.toTimeAgo()

            rlRepostRoot.setOnClickListener(onClickListener)
            rlRepostRoot.tag = this@NeedViewHolder
        }
    }

    companion object {
        fun newInstance(
                parent: ViewGroup,
                onClickListener: View.OnClickListener,
                imagesCount: Int,
                isRepost: Boolean,
                isPromoted: Boolean
        ): NeedViewHolder {

            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_news_feed_need, parent, false)
            view.rlRepostRoot.isGone = !isRepost
            view.flImagesRoot.isGone = imagesCount == 0
            view.llPromotedRoot.isGone = !isPromoted

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