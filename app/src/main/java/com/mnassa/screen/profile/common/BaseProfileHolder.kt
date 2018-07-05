package com.mnassa.screen.profile.common

import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.TextView
import com.mnassa.R
import com.mnassa.domain.model.ConnectionStatus
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.screen.posts.need.details.adapter.PostTagRVAdapter
import com.mnassa.translation.fromDictionary
import com.mnassa.widget.FlowLayout
import com.mnassa.widget.SimpleChipView
import java.text.DateFormatSymbols
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */
abstract class BaseProfileHolder(itemView: View) : BasePaginationRVAdapter.BaseVH<PostModel>(itemView) {

    protected var bottomTagsAdapter = PostTagRVAdapter()

    abstract fun bindProfile(profile: ProfileAccountModel)
    abstract fun bindOffers(offers: List<TagModel>)
    abstract fun bindInterests(interests: List<TagModel>)
    abstract fun bindConnectionStatus(connectionStatus: ConnectionStatus)

    protected fun getSpannableText(count: String, text: String, color: Int): SpannableString {
        val value = "$count\n$text"
        val span = SpannableString(value)
        span.setSpan(ForegroundColorSpan(color), START_SPAN, count.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(RelativeSizeSpan(PROPORTION_TEXT_SIZE), START_SPAN, count.length, 0)
        return span
    }

    protected fun setCheckedTags(tvLabel: TextView, flowLayout: FlowLayout, topView: View?, tags: List<TagModel>?, text: String) {
        tags?.let {
            tvLabel.text = text
            for (tag in tags) {
                flowLayout.visibility = View.VISIBLE
                tvLabel.visibility = View.VISIBLE
                topView?.visibility = View.VISIBLE
                val chipView = SimpleChipView(flowLayout.context, tag)
                val params = FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT)
                chipView.layoutParams = params
                flowLayout.addView(chipView)
            }
        } ?: kotlin.run {
            flowLayout.visibility = View.GONE
            tvLabel.visibility = View.GONE
            topView?.visibility = View.GONE
        }

    }

    protected fun onMoreClick(profileInfo: View, llBottomTags: View, tvMoreInformation: TextView, vBottomDivider: View, areThereTags: Boolean) {
        profileInfo.visibility = if (profileInfo.visibility == View.GONE) View.VISIBLE else View.GONE
        llBottomTags.visibility = if (llBottomTags.visibility == View.GONE) View.VISIBLE else View.GONE
        val drawable = if (profileInfo.visibility == View.GONE) R.drawable.ic_down else R.drawable.ic_up
        val img = ResourcesCompat.getDrawable(tvMoreInformation.resources, drawable, null)
        tvMoreInformation.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
        tvMoreInformation.text = if (profileInfo.visibility == View.GONE) {
            vBottomDivider.visibility = if (areThereTags) View.GONE else View.VISIBLE
            fromDictionary(R.string.profile_more_information)
        } else {
            vBottomDivider.visibility = View.VISIBLE
            fromDictionary(R.string.profile_less_information)
        }
    }

    protected fun handleConnection(textView: TextView, connectionStatus: ConnectionStatus) {
        textView.text = when (connectionStatus) {
            ConnectionStatus.CONNECTED ->
                getSpannableText(EMPTY_CONNECTIONS_TEXT,
                        fromDictionary(R.string.user_profile_connection_connected),
                        ContextCompat.getColor(textView.context, R.color.green_cool))
            ConnectionStatus.REQUESTED ->
                getSpannableText(EMPTY_CONNECTIONS_TEXT,
                        fromDictionary(R.string.user_profile_connection_connect),
                        ContextCompat.getColor(textView.context, R.color.green_cool))
            ConnectionStatus.RECOMMENDED ->
                getSpannableText(EMPTY_CONNECTIONS_TEXT,
                        fromDictionary(R.string.user_profile_connection_connect),
                        ContextCompat.getColor(textView.context, R.color.green_cool))
            ConnectionStatus.SENT ->
                getSpannableText(EMPTY_CONNECTIONS_TEXT,
                        fromDictionary(R.string.profile_request_was_sent),
                        ContextCompat.getColor(textView.context, R.color.gray_cool))
            else -> null
        }
    }

    protected fun setCheckedTexts(tvLabel: TextView, tvText: TextView, topView: View?, hint: String, text: String?) {
        if (TextUtils.isEmpty(text)) {
            topView?.visibility = View.GONE
            tvLabel.visibility = View.GONE
            tvText.visibility = View.GONE
            return
        }
        topView?.visibility = View.VISIBLE
        tvLabel.visibility = View.VISIBLE
        tvText.visibility = View.VISIBLE
        tvLabel.text = hint
        tvText.text = text
    }

    protected fun getDateByTimeMillis(createdAt: Long?): String? {
        val cal = Calendar.getInstance()
        cal.timeInMillis = createdAt ?: return null
        return "${DateFormatSymbols().months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
    }

    companion object {
        const val START_SPAN = 0
        const val PROPORTION_TEXT_SIZE = 1.5f
        const val EMPTY_CONNECTIONS_TEXT = " "
    }

}