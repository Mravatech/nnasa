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
 * Date: 3/26/2018
 */

@SuppressLint("ViewConstructor")
class SimpleChipView(
        context: Context,
        tagModel: TagModel) : FrameLayout(context) {


    init {
        inflate(context, R.layout.view_simple_chip, this)
        tvChipText.text = tagModel.name
    }

}