package com.mnassa.extensions

import android.view.inputmethod.EditorInfo
import android.widget.EditText

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