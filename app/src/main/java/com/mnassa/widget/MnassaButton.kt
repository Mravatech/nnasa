package com.mnassa.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.AppCompatButton
import android.util.AttributeSet
import com.mnassa.R
import com.mnassa.translation.fromDictionary

/**
 * Created by Peter on 8/23/2018.
 */
class MnassaButton : AppCompatButton {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, android.R.attr.buttonStyle)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        attrs ?: return
        val attributeArray = context.obtainStyledAttributes(attrs, R.styleable.MnassaTextView, defStyleAttr, 0)
        try {
            text = attributeArray.getString(R.styleable.MnassaTextView_textDictionary)?.let { fromDictionary(it) }

            var drawableStart: Drawable? = null
            var drawableEnd: Drawable? = null
            var drawableBottom: Drawable? = null
            var drawableTop: Drawable? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawableStart = attributeArray.getDrawable(R.styleable.MnassaTextView_drawableStartCompat)
                drawableEnd = attributeArray.getDrawable(R.styleable.MnassaTextView_drawableEndCompat)
                drawableBottom = attributeArray.getDrawable(R.styleable.MnassaTextView_drawableBottomCompat)
                drawableTop = attributeArray.getDrawable(R.styleable.MnassaTextView_drawableTopCompat)
            } else {
                val drawableStartId = attributeArray.getResourceId(R.styleable.MnassaTextView_drawableStartCompat, -1)
                val drawableEndId = attributeArray.getResourceId(R.styleable.MnassaTextView_drawableEndCompat, -1)
                val drawableBottomId = attributeArray.getResourceId(R.styleable.MnassaTextView_drawableBottomCompat, -1)
                val drawableTopId = attributeArray.getResourceId(R.styleable.MnassaTextView_drawableTopCompat, -1)

                if (drawableStartId != -1)
                    drawableStart = AppCompatResources.getDrawable(context, drawableStartId)
                if (drawableEndId != -1)
                    drawableEnd = AppCompatResources.getDrawable(context, drawableEndId)
                if (drawableBottomId != -1)
                    drawableBottom = AppCompatResources.getDrawable(context, drawableBottomId)
                if (drawableTopId != -1)
                    drawableTop = AppCompatResources.getDrawable(context, drawableTopId)
            }

            // to support rtl
            setCompoundDrawablesRelativeWithIntrinsicBounds(drawableStart, drawableTop, drawableEnd, drawableBottom)
        } finally {
            attributeArray.recycle()
        }
    }
}