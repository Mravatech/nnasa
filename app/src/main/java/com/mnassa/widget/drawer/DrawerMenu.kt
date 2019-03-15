package com.mnassa.widget.drawer

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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

        isClickable = true

        setOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }
    }
}