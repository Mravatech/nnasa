package com.mnassa.widget.input

import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.design.widget.TextInputEditText
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.view.MotionEvent
import com.mnassa.R


/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/15/2018
 */

class ClickableDrawableTextInputEditText : TextInputEditText {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var isChosen = false

    init {
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (right - compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                    when (isChosen) {
                        true -> onDrawableRightClick(R.color.black, R.drawable.ic_eye_on)
                        false -> onDrawableRightClick(R.color.gray_cool, R.drawable.ic_eye_off)
                    }
                }
            }
            false
        }
        onDrawableRightClick(R.color.gray_cool, R.drawable.ic_eye_off)
    }

    fun setHideMode(hasToHide: Boolean) {
        isChosen = hasToHide
        when (hasToHide) {
            true -> onDrawableRightClick(R.color.black, R.drawable.ic_eye_on)
            false -> onDrawableRightClick(R.color.coolGray, R.drawable.ic_eye_off)
        }
    }

    private fun onDrawableRightClick(@ColorRes color: Int, @DrawableRes drawable: Int) {
        isFocusable = isChosen
        isFocusableInTouchMode = isChosen
        isLongClickable = isChosen
        isChosen = !isChosen
        setTextColor(ContextCompat.getColor(context, color))
        val img = ResourcesCompat.getDrawable(resources, drawable, null)
        setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
    }

    companion object {
        const val DRAWABLE_RIGHT = 2
    }

}