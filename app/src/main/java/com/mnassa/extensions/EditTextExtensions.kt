package com.mnassa.extensions

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView

/**
 * Created by Peter on 3/1/2018.
 */
fun EditText.onImeActionDone(function: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            function()
            true
        } else false
    }
}

fun TextView.invisibleIfEmpty() {
    visibility = if (text.isNullOrBlank()) View.INVISIBLE else View.VISIBLE
}

fun TextView.goneIfEmpty() {
    visibility = if (text.isNullOrBlank()) View.GONE else View.VISIBLE
}