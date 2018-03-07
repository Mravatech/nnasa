package com.mnassa.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.bluelinelabs.conductor.Controller

/**
 * Created by Peter on 3/7/2018.
 */
fun Controller.hideKeyboard(viewParam: View? = null) {
    // Check if no view has focus:
    val activity = activity ?: return
    val view = viewParam ?: activity.currentFocus
    if (view != null) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun showKeyboard(viewParam: View) {
    viewParam.requestFocus()

    viewParam.postDelayed({
        val keyboard = viewParam.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.showSoftInput(viewParam, InputMethodManager.SHOW_IMPLICIT)
    }, 200)
}