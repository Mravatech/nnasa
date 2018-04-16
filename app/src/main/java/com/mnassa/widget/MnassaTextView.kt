package com.mnassa.widget

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import com.mnassa.translation.fromDictionary

/**
 * Created by Peter on 3/30/2018.
 */
class MnassaTextView : AppCompatTextView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (text != null) {
            super.setText(fromDictionary(text.toString()), type)
        } else super.setText(text, type)
    }
}