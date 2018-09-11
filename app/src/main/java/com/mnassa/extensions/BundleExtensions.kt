package com.mnassa.extensions

import android.os.Bundle

/**
 * Created by Peter on 9/11/2018.
 */
inline fun bundleOf(block: Bundle.() -> Unit): Bundle {
    val bundle = Bundle()
    block(bundle)
    return bundle
}