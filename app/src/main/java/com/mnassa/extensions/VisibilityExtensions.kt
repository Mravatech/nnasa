package com.mnassa.extensions

import android.view.View

/**
 * Created by Peter on 3/28/2018.
 */
var View.isGone: Boolean
    set(value) {
        visibility = if (value) View.GONE else View.VISIBLE
    }
    get() = visibility == View.GONE

var View.isInvisible: Boolean
    set(value) {
        visibility = if (value) View.INVISIBLE else View.VISIBLE
    }
    get() = visibility == View.INVISIBLE