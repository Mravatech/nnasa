package com.mnassa.widget.search

import android.content.Context
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
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflate(context, R.layout.search_view, this)
        ivSearchClose.setOnClickListener {
            if (etSearch.text.toString().isNotEmpty()){
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

}