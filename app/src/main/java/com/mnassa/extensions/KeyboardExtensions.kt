package com.mnassa.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.bluelinelabs.conductor.Controller
import timber.log.Timber

/**
 * Created by Peter on 3/7/2018.
 */
fun Controller.hideKeyboard(view: View? = null) = activity?.hideKeyboard(view)

fun Activity.hideKeyboard(view: View? = null) {

    val viewInner = view ?: currentFocus ?: return
    viewInner.postDelayed({
        try {
            viewInner.clearFocus()
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(viewInner.windowToken, 0)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }, 200)

}

fun showKeyboard(view: View) {
//    view.postDelayed({
        try {
//            view.requestFocus()
//            val keyboard = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            keyboard.showSoftInput(view, 0)
        } catch (e: Exception) {
            Timber.e(e)
        }
//    }, 200)
}