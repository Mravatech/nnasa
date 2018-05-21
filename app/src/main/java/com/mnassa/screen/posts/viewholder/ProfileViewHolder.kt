package com.mnassa.screen.posts.viewholder

import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.RecommendedProfilePostModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.*
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_news_feed_profile.view.*
import kotlinx.android.synthetic.main.recommended_profile.view.*

/**
 * Created by Peter on 3/14/2018.
 */
class ProfileViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<PostModel>(itemView) {

    override fun bind(item: PostModel) {
        with(itemView) {
            ivAvatar.avatarRound(item.author.avatar)

            tvUserName.text = item.author.formattedName
            tvRecommends.text = fromDictionary(R.string.posts_recommends)
            tvTime.text = item.originalCreatedAt.toTimeAgo()
            tvDescription.text = item.formattedText
            tvDescription.goneIfEmpty()

            tvViewsCount.text = item.counters.views.toString()
            tvCommentsCount.text = item.counters.comments.toString()

            rlClickableRoot.setOnClickListener(onClickListener)
            rlClickableRoot.tag = this@ProfileViewHolder

            btnAction.visibility = if (item.autoSuggest.youCanHelp || item.autoSuggest.accountIds.isNotEmpty()) View.VISIBLE else View.GONE
            btnAction.text = when {
                item.autoSuggest.youCanHelp && item.autoSuggest.accountIds.isNotEmpty() ->
                    fromDictionary(R.string.need_item_btn_you_and_connections_can_help).format(item.autoSuggest.accountIds.size)
                item.autoSuggest.youCanHelp -> fromDictionary(R.string.need_item_btn_you_can_help)
                item.autoSuggest.accountIds.isNotEmpty() -> fromDictionary(R.string.need_item_btn_connections_can_help).format(item.autoSuggest.accountIds.size)
                else -> null
            }

            btnAction.setOnClickListener(onClickListener)
            btnAction.tag = this@ProfileViewHolder

            rlAuthorRoot.setOnClickListener(onClickListener)
            rlAuthorRoot.tag = this@ProfileViewHolder

            bindRepost(item)

            val recommended = (item as RecommendedProfilePostModel).recommendedProfile
            ivRecommendedUserAvatar.avatarSquare(recommended?.avatar)
            tvRecommendedUserName.text = recommended?.formattedName ?:  fromDictionary(R.string.deleted_user)
            tvRecommendedUserPosition.text = recommended?.formattedPosition
            tvRecommendedUserPosition.goneIfEmpty()

            if (item.offers.isNotEmpty()) {
                val prefix = fromDictionary(R.string.posts_recommends_skills_prefix)
                tvUserTags.text = prefix
                tvUserTags.append(" ")
                tvUserTags.append(item.offers.joinToString { it.name })
            } else {
                tvUserTags.text = null
            }
            tvUserTags.goneIfEmpty()

            vDescriptionDivider.visibility = if (tvDescription.visibility == View.VISIBLE && tvUserTags.visibility == View.VISIBLE) View.VISIBLE else View.GONE
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
            rlRepostRoot.tag = this@ProfileViewHolder
        }
    }

    companion object {
        fun newInstance(
                parent: ViewGroup,
                onClickListener: View.OnClickListener,
                isPromoted: Boolean): ProfileViewHolder {

            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news_feed_profile, parent, false)
            view.rlRecommendedProfileRoot.visibility = View.VISIBLE
            view.llPromotedRoot.isGone = !isPromoted

            return ProfileViewHolder(view, onClickListener)
        }
    }
}