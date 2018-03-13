package com.mnassa.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.mnassa.R
import kotlinx.android.synthetic.main.header_main.view.*
import kotlinx.android.synthetic.main.red_badge.view.*

/**
 * Created by Peter on 3/13/2018.
 */
class MnassaToolbar : FrameLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val horizontalSpacing by lazy { resources.getDimensionPixelSize(R.dimen.spacing_horizontal) }
    private val verticalSpacing by lazy { resources.getDimensionPixelSize(R.dimen.spacing_vertical) }

    init {
        val innerView = LayoutInflater.from(context).inflate(R.layout.header_main, null)
        addView(innerView)

        ivToolbarMore.setOnClickListener { onMoreClickListener?.invoke(it) }
    }

    var backButtonEnabled: Boolean
        get() = ivToolbarBack.visibility == View.VISIBLE
        set(value) {
            ivToolbarBack.visibility = if (value) View.VISIBLE else View.GONE
            val titleMargin = if (value) 0 else horizontalSpacing

            val layoutParams = tvToolbarScreenHeader.layoutParams as MarginLayoutParams
            layoutParams.marginStart = titleMargin
            tvToolbarScreenHeader.layoutParams = layoutParams
        }

    var title: String
        get() = tvToolbarScreenHeader.text.toString()
        set(value) {
            tvToolbarScreenHeader.text = value
        }

    var counter: Int
        get() {
            return if (toolbarBadge.visibility == View.VISIBLE) {
                tvBadgeCount.text.toString().toIntOrNull() ?: 0
            } else 0
        }
        set(value) {
            tvBadgeCount.text = value.toString()
            toolbarBadge.visibility = if (value == 0) View.GONE else View.VISIBLE
        }

    var onMoreClickListener : ((View) -> Unit)? = null
        set(value) {
            field = value
            ivToolbarMore.visibility = if (value != null) View.VISIBLE else View.GONE
        }

}