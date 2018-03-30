package com.mnassa.screen.profile.common

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.TextView
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.screen.profile.model.ProfileModel
import com.mnassa.widget.FlowLayout
import com.mnassa.widget.SimpleChipView
import java.text.DateFormatSymbols
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */
abstract class BaseProfileHolder(itemView: View) : BasePaginationRVAdapter.BaseVH<ProfileModel>(itemView) {

    protected fun getSpannableText(count: String, text: String): SpannableString {
        val value = "$count\n$text"
        val span = SpannableString(value)
        span.setSpan(ForegroundColorSpan(Color.BLACK), 0, count.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(RelativeSizeSpan(1.5f), 0, count.length, 0)
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

    protected fun setCheckedTexts(tvLabel: TextView, tvText: TextView, topView: View?, hint: String, text: String?) {
        text?.let {
            topView?.visibility = View.VISIBLE
            tvLabel.visibility = View.VISIBLE
            tvText.visibility = View.VISIBLE
            tvLabel.text = hint
            tvText.text = it
        } ?: run {
            topView?.visibility = View.GONE
            tvLabel.visibility = View.GONE
            tvText.visibility = View.GONE
        }
    }

    protected fun getDateByTimeMillis(createdAt: Long?): String? {
        val cal = Calendar.getInstance()
        cal.timeInMillis = createdAt ?: return null
        return "${DateFormatSymbols().months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
    }

}