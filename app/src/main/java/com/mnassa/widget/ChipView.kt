package com.mnassa.widget

import android.annotation.SuppressLint
import android.content.Context
import android.widget.FrameLayout
import com.mnassa.R
import com.mnassa.domain.model.TagModel
import kotlinx.android.synthetic.main.view_chip.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/9/2018
 */

@SuppressLint("ViewConstructor")
class ChipView(
        context: Context,
        tagModel: TagModel,
        private val key: Long,
        private val onChipListener: OnChipListener
) : FrameLayout(context) {

    init {
        inflate(context, R.layout.view_chip, this)
        tvChipText.text = tagModel.name.toString()
        ivRemoveTag.setOnClickListener {
            removeViewFromParent()
        }
    }

    fun removeViewFromParent() {
        val parentViewGroup: FlowLayout = parent as FlowLayout
        parentViewGroup.removeView(this)
        onChipListener.onViewRemoved(key)
    }

    interface OnChipListener {
        fun onViewRemoved(key: Long)
    }
}