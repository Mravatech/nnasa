package com.mnassa.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.mnassa.R
import com.mnassa.domain.model.TagModelTemp
import kotlinx.android.synthetic.main.view_chip.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/9/2018
 */

@SuppressLint("ViewConstructor")
class ChipView(
        context: Context?,
        tagModel: TagModelTemp,
        private val key: Long,
        private val onChipListener: OnChipListener
) : LinearLayout(context) {

    init {
        View.inflate(context, R.layout.view_chip, this)
        tvChipText.text = tagModel.name
        ibRemove.setOnClickListener {
            removeViewFromParent()
        }
    }

    fun removeViewFromParent() {
        val v: FlowLayout = this@ChipView.parent as FlowLayout
        v.removeView(this@ChipView)
        onChipListener.onViewRemoved(key)
    }

    interface OnChipListener {
        fun onViewRemoved(key: Long)
    }

}