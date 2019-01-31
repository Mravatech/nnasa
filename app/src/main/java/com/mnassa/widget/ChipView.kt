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
class ChipView(context: Context, val tagModel: TagModel, private val onRemove: (TagModel) -> Unit) : FrameLayout(context) {

    init {
        inflate(context, R.layout.view_chip, this)
        bind(tagModel)
    }

    private fun bind(tag: TagModel) {
        tvChipText.text = tag.name.toString()
        ivRemoveTag.setOnClickListener {
            onRemove(tag)
        }
    }
}