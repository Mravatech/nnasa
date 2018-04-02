package com.mnassa.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.mnassa.R
import kotlinx.android.synthetic.main.header_main.view.*
import kotlinx.android.synthetic.main.red_badge.view.*
import android.content.res.TypedArray
import android.os.Build
import android.support.annotation.RequiresApi
import com.mnassa.translation.fromDictionary


/**
 * Created by Peter on 3/13/2018.
 */
class MnassaToolbar : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        processAttrs(attrs, 0, 0)
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
       processAttrs(attrs, defStyleAttr, 0)
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        processAttrs(attrs, defStyleAttr, defStyleRes)
    }

    private val horizontalSpacing by lazy { resources.getDimensionPixelSize(R.dimen.spacing_horizontal) }
    private val verticalSpacing by lazy { resources.getDimensionPixelSize(R.dimen.spacing_vertical) }

    init {
        val innerView = LayoutInflater.from(context).inflate(R.layout.header_main, null)
        addView(innerView)

        ivToolbarMore.setOnClickListener { onMoreClickListener?.invoke(it) }
    }

    private fun processAttrs(attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.MnassaToolbar, 0, 0)

        attributes.getResourceId(R.styleable.MnassaToolbar_toolbar_title, -1).takeIf { it != -1 }?.let {
            title = fromDictionary(it)
        }
        shadowEnabled = attributes.getBoolean(R.styleable.MnassaToolbar_shadow_enabled, true)
        backButtonEnabled = attributes.getBoolean(R.styleable.MnassaToolbar_back_button_enabled, true)
        drawerButtonEnabled = attributes.getBoolean(R.styleable.MnassaToolbar_drawer_button_enabled, false)

        attributes.recycle()
    }

    var backButtonEnabled: Boolean
        get() = ivToolbarBack.visibility == View.VISIBLE
        set(value) {
            if (value) drawerButtonEnabled = false
            ivToolbarBack.visibility = if (value) View.VISIBLE else View.GONE
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

    var shadowEnabled: Boolean
        get() = vShadow.visibility == View.VISIBLE
        set(value) {
            vShadow.visibility = if (value) View.VISIBLE else View.INVISIBLE
        }

    var drawerButtonEnabled: Boolean
        get() = ivToolbarDrawer.visibility == View.VISIBLE
        set(value) {
            if (value) backButtonEnabled = false
            ivToolbarDrawer.visibility = if (value) View.VISIBLE else View.GONE
        }

    fun withActionButton(actionText: String, listener: (View) -> Unit) {
        btnAction.visibility = View.VISIBLE
        btnAction.text = actionText
        btnAction.setOnClickListener { listener(it) }
    }
    var actionButtonEnabled: Boolean
        get() = btnAction.visibility == View.VISIBLE
        set(value) {
            btnAction.visibility = if (value) View.VISIBLE else View.GONE
        }
    var actionButtonClickable: Boolean
        get() = btnAction.isEnabled
        set(value) {
            btnAction.isEnabled = value
        }

}