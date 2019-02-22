package com.mnassa.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.mnassa.R

class PrefixEditText : AppCompatEditText {
    private var mOriginalLeftPadding = -1f
    var prefix: String = ""
        set(value) {
            field = value
            invalidate()
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inputType = inputType or EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        calculatePrefix()
    }

    private fun calculatePrefix() {
        if (mOriginalLeftPadding == -1f) {
            val widths = FloatArray(prefix.length)
            paint.getTextWidths(prefix, widths)
            var textWidth = 0f
            for (w in widths) {
                textWidth += w
            }
            mOriginalLeftPadding = compoundPaddingLeft.toFloat()
            setPadding((textWidth + mOriginalLeftPadding).toInt(),
                    paddingRight, paddingTop,
                    paddingBottom)
        }
    }

    private val prefixPaint by lazy {
        val result = Paint(paint)
        result.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        result.color = ContextCompat.getColor(context, R.color.black)
        result
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawText(
                prefix,
                mOriginalLeftPadding,
                getLineBounds(0, null).toFloat(),
                prefixPaint)
    }
}