package com.mnassa.screen.posts.viewholder

import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.TypefaceCompat
import android.support.v4.graphics.TypefaceCompatUtil
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_news_feed_header.view.*

/**
 * Created by Peter on 25.03.2018.
 */
class HeaderViewHolder(itemView: View) : BasePaginationRVAdapter.BaseVH<Any>(itemView) {
    override fun bind(item: Any) {

    }

    companion object {
        fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): HeaderViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news_feed_header, parent, false)
            val viewHolder =  HeaderViewHolder(view)

            val text = SpannableStringBuilder(fromDictionary(R.string.posts_header_need))
            text.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            val black = ContextCompat.getColor(parent.context, R.color.black)
            text.setSpan(ForegroundColorSpan(black), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            text.append(" ")

            val tail = fromDictionary(R.string.posts_header_type)
            val startTailSpan = text.length
            val tailColor = ContextCompat.getColor(parent.context, R.color.coolGray)
            text.append(tail)
            text.setSpan(ForegroundColorSpan(tailColor), startTailSpan, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            view.tvINeedTitle.text = text

            view.flCreateNeed.setOnClickListener(onClickListener)
            view.flCreateNeed.tag = viewHolder

            return viewHolder
        }
    }
}