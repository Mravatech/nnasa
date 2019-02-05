package com.mnassa.widget.drawer

import android.content.Context
import android.os.Build
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.core.view.GravityCompat
import com.mnassa.R

/**
 * Created by Peter on 3/19/2018.
 */
class DrawerMenu : AppCompatImageView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val drawer: DrawerLayout
        get() {
            var parent = this.parent
            while (parent != null && parent !is DrawerLayout) {
                parent = parent.parent
            }
            return requireNotNull(parent) as DrawerLayout
        }

    init {
        setImageResource(R.drawable.ic_menu_white_24dp)

        val outValue = TypedValue()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            setBackgroundResource(outValue.resourceId)
        }

        isClickable = true

        setOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }
    }
}