package com.mnassa.widget.linearchip

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.mnassa.R


class CountChipView : FrameLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var tag: TextView

    init {
        View.inflate(context, R.layout.count_chip_view, this)
        tag = findViewById(R.id.tvCount)
    }

    fun setText(text: String) {
        tag.text = text
    }

}