package com.mnassa.widget.text

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import com.mnassa.R
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.end_prefix_view.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 5/7/2018
 */

class EndPrefixEditText : LinearLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        orientation = VERTICAL
        inflate(context, R.layout.end_prefix_view, this)
        tvFloatingLabel.text = fromDictionary(R.string.post_expires_in)
        etEditableText.setText(fromDictionary(R.string.post_expires_day_s))
        tvPrefix.text = fromDictionary(R.string.post_expires_day_s)
        etEditableText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val colorRes = ContextCompat.getColor(context, R.color.accent)
                tvFloatingLabel.setTextColor(colorRes)
                vFakeBottomView.setBackgroundColor(colorRes)
            } else {
                val colorRes = ContextCompat.getColor(context, R.color.chip_edit_text_hint)
                tvFloatingLabel.setTextColor(colorRes)
                vFakeBottomView.setBackgroundColor(colorRes)
            }
        }
        this.setOnClickListener { etEditableText.requestFocus() }
    }


}