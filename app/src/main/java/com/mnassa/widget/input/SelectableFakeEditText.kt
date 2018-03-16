package com.mnassa.widget.input

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.mnassa.R

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/16/2018
 */

class SelectableFakeEditText : LinearLayout {


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflate(context, R.layout.selectable_fake_edit_text, this)
    }


}