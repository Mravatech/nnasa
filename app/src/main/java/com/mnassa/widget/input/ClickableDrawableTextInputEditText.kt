package com.mnassa.widget.input

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textfield.TextInputEditText
import com.mnassa.R


/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/15/2018
 */

class ClickableDrawableTextInputEditText : TextInputEditText {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        attrs ?: return
        val attributeArray = context.obtainStyledAttributes(attrs, R.styleable.MnassaTextView, defStyleAttr, 0)
        try {
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

    var isChosen = false

    init {
        setOnTouchListener { _, event ->
            var result = false
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (right - compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                    setHideMode(!isChosen)
                    result = true
                }
            }
            result
        }
        setHideMode(false)
    }

    fun setHideMode(hasToHide: Boolean) {
        isChosen = hasToHide
        when (hasToHide) {
            true -> onDrawableRightClick(R.color.black, R.drawable.ic_eye_on)
            false -> onDrawableRightClick(R.color.gray_cool, R.drawable.ic_eye_off)
        }
    }

    private fun onDrawableRightClick(@ColorRes color: Int, @DrawableRes drawable: Int) {
        val img = ResourcesCompat.getDrawable(resources, drawable, null)
        setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
    }

    companion object {
        const val DRAWABLE_RIGHT = 2
    }

}