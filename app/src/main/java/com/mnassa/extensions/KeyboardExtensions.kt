package com.mnassa.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.bluelinelabs.conductor.Controller

/**
 * Created by Peter on 3/7/2018.
 */
fun Controller.hideKeyboard(view: View? = null) {
    // Check if no view has focus:
    val activity = activity ?: return
    val viewInner = view ?: activity.currentFocus
    viewInner?.let {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun Activity.hideKeyboard(view: View? = null) {
    val viewInner = view ?: currentFocus
    viewInner?.let {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun showKeyboard(viewParam: View) {
    viewParam.requestFocus()

    viewParam.postDelayed({
        val keyboard = viewParam.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.showSoftInput(viewParam, InputMethodManager.SHOW_IMPLICIT)
    }, 200)
}