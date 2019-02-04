package com.mnassa.extensions

import android.text.Editable

val Editable?.lengthOrZero: Int
    get() = this?.length ?: 0
