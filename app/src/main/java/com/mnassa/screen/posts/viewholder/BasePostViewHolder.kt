package com.mnassa.screen.posts.viewholder

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mnassa.R
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.formattedName
import com.mnassa.extensions.image
import com.mnassa.extensions.isRepost
import com.mnassa.extensions.toTimeAgo
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary

/**
 * Created by Peter on 5/24/2018.
 */
abstract class BasePostViewHolder(itemView: View, val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<PostModel>(itemView) {

    protected fun bindImages(item: PostModel) {
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

                itemView.findViewById<TextView>(R.id.tvCountMore).text = String.format("+%s", item.attachments.size - 2)
            }
        }
    }

    protected fun bindRepost(item: PostModel) {
        if (!item.isRepost) return

        with(itemView) {
            val repostedBySpan = SpannableStringBuilder(fromDictionary(R.string.need_item_reposted_by))
            repostedBySpan.append(" ")
            val startSpan = repostedBySpan.length
            repostedBySpan.append(requireNotNull(item.repostAuthor).formattedName)

            repostedBySpan.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View?) {
                    onClickListener.onClick(findViewById(R.id.rlRepostRoot))
                }
            }, startSpan, repostedBySpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            val tvReplyText = findViewById<TextView>(R.id.tvReplyText)
            tvReplyText.text = repostedBySpan
            tvReplyText.movementMethod = LinkMovementMethod.getInstance()

            findViewById<TextView>(R.id.tvReplyTime).text = item.createdAt.toTimeAgo()

            findViewById<View>(R.id.rlRepostRoot).tag = this@BasePostViewHolder
        }
    }

    protected fun bindGroup(item: PostModel) {
        if (item.groups.isEmpty()) return

        with(itemView) {
            val tvGroupText: TextView = findViewById(R.id.tvGroupText)

            val groupSpan = SpannableStringBuilder(fromDictionary(R.string.need_item_from_group))
            groupSpan.append(" ")
            var startSpan = groupSpan.length

            item.groups.forEachIndexed { index, group ->
                groupSpan.append(group.formattedName)
                groupSpan.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View?) {
                        tvGroupText.tag = group
                        onClickListener.onClick(tvGroupText)
                    }
                }, startSpan, groupSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                if (index != (item.groups.size - 1)) {
                    groupSpan.append(", ")
                }

                startSpan = groupSpan.length
            }

            tvGroupText.text = groupSpan
            tvGroupText.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}