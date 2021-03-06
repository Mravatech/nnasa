package com.mnassa.widget.text

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.mnassa.R
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.end_prefix_view.view.*
import java.util.concurrent.TimeUnit
import com.mnassa.extensions.addTextChangedListener


/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 5/7/2018
 */

class EndPrefixEditText : LinearLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var dayTo: String? = null
        set(value) {
            field = value
            etEditableText.setText(value)
        }

    var millisTo: Long? = null
        get() {
            val timeTo = etEditableText.text.toString().toLongOrNull() ?: return null
            return System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timeTo, TimeUnit.DAYS)
        }
        private set

    init {
        orientation = VERTICAL
        inflate(context, R.layout.end_prefix_view, this)
        tvFloatingLabel.text = fromDictionary(R.string.post_expires_in)
        tvPrefix.text = fromDictionary(R.string.post_expires_day_s).replace("%d", "")
        etEditableText.addTextChangedListener {
            tvEditableTextGhost.text = it
        }
        etEditableText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val colorRes = ContextCompat.getColor(context, R.color.accent)
                tvFloatingLabel.setTextColor(colorRes)
            } else {
                val colorRes = ContextCompat.getColor(context, R.color.chip_edit_text_hint)
                tvFloatingLabel.setTextColor(colorRes)
            }
        }
    }


}