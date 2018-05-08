package com.mnassa.widget.search

import android.content.Context
import android.graphics.Color
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.mnassa.R
import com.mnassa.extensions.hideKeyboard
import com.mnassa.extensions.showKeyboard
import kotlinx.android.synthetic.main.search_view.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 5/8/2018
 */
class MnassaSearchView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        processAttrs(attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        processAttrs(attrs, defStyleAttr, 0)
    }

    init {
        inflate(context, R.layout.search_view, this)
        ivSearchClose.setOnClickListener {
            if (etSearch.text.toString().isNotEmpty()) {
                etSearch.text = null
                return@setOnClickListener
            }
            etSearch.visibility = View.GONE
            ivSearchClose.visibility = View.GONE
            etSearch.hideKeyboard()
        }
        ivSearch.setOnClickListener {
            etSearch.visibility = View.VISIBLE
            ivSearchClose.visibility = View.VISIBLE
            showKeyboard(etSearch)
        }
    }

    private fun processAttrs(attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.MnassaSearchView, 0, 0)
        val color = attributes.getColor(R.styleable.MnassaSearchView_icons_color, Color.WHITE)
        DrawableCompat.setTint(ivSearch.drawable, color)
        DrawableCompat.setTint(ivSearchClose.drawable, color)
        etSearch.setTextColor(color)
        attributes.recycle()
    }

}