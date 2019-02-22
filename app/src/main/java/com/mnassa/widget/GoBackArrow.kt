package com.mnassa.widget

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.mnassa.R

/**
 * Created by Peter on 12.03.2018.
 */
class GoBackArrow : AppCompatImageView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setImageResource(R.drawable.ic_arrow_back_white_24dp)

        val outValue = TypedValue()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            setBackgroundResource(outValue.resourceId)
        }

        isClickable = true

        setOnClickListener {
            val context = it.context
            if (context is Activity) {
                context.onBackPressed()
            } else if (context is AppCompatActivity) {
                context.onBackPressed()
            }
        }
    }
}