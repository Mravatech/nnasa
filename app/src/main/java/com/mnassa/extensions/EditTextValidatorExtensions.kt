package com.mnassa.extensions

import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.EditText
import java.util.regex.Pattern

/**
 * Created by Peter on 2/28/2018.
 */

//TODO: pass error text as function parameter (?)

val PATTERN_PHONE_TAIL = Pattern.compile("\\d{12,13}")!!

fun TextInputLayout.validateAsFirstName(): Boolean {
    val editText = findEditText()
    val text = editText.text.toString()
    isErrorEnabled = true
    when {
        text.isEmpty() -> error = "Enter first name"
        else -> return true
    }
    editText.addTextChangedListener {
        error = ""                              //TODO: validate again or validate after button click?
                                                //TODO: fix index out of bounds here
                                                //TODO: fix multiple text change listeners
        editText.removeTextChangedListener(this)
    }
    return false
}

fun TextInputLayout.validateAsLastName(): Boolean {
    val editText = findEditText()
    val text = editText.text.toString()
    isErrorEnabled = true
    when {
        text.isEmpty() -> error = "Enter last name"
        else -> return true
    }
    editText.addTextChangedListener {
        error = ""
        editText.removeTextChangedListener(this)
    }
    return false
}

fun TextInputLayout.validateAsUserName(): Boolean {
    val editText = findEditText()
    val text = editText.text.toString()
    isErrorEnabled = true
    when {
        text.isEmpty() -> error = "Enter user name"
        else -> return true
    }
    editText.addTextChangedListener {
        error = ""
        editText.removeTextChangedListener(this)
    }
    return false
}

fun TextInputLayout.validateAsCity(): Boolean {
    val editText = findEditText()
    val text = editText.text.toString()
    isErrorEnabled = true
    when {
        text.isEmpty() -> error = "Enter city"
        else -> return true
    }
    editText.addTextChangedListener {
        error = ""
        editText.removeTextChangedListener(this)
    }
    return false
}

fun TextInputLayout.validateAsOffers(): Boolean {
    val editText = findEditText()
    val text = editText.text.toString()
    isErrorEnabled = true
    when {
        text.isEmpty() -> error = "Enter offers"
        else -> return true
    }
    editText.addTextChangedListener {
        error = ""
        editText.removeTextChangedListener(this)
    }
    return false
}

fun TextInputLayout.validateAsInterests(): Boolean {
    val editText = findEditText()
    val text = editText.text.toString()
    isErrorEnabled = true
    when {
        text.isEmpty() -> error = "Enter offers"
        else -> return true
    }
    editText.addTextChangedListener {
        error = ""
        editText.removeTextChangedListener(this)
    }
    return false
}

fun TextInputLayout.validateAsCompanyName(): Boolean {
    val editText = findEditText()
    val text = editText.text.toString()
    isErrorEnabled = true
    when {
        text.isEmpty() -> error = "Enter company name"
        else -> return true
    }
    editText.addTextChangedListener {
        error = ""
        editText.removeTextChangedListener(this)
    }
    return false
}

private fun TextInputLayout.findEditText(viewGroup: ViewGroup? = null): EditText {
    val layout = viewGroup ?: this
    (0 until layout.childCount)
            .map { layout.getChildAt(it) }
            .forEach {
                if (it is EditText) {
                    return it
                } else if (it is ViewGroup) {
                    return findEditText(it)
                }
            }
    throw IllegalStateException("TextInputLayout must contain Edit Text!")
}


fun EditText.addTextChangedListener(listener: TextWatcher.(text: String) -> Unit): TextWatcher {
    val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) = Unit
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            listener.invoke(this, s?.toString() ?: "")
        }
    }
    addTextChangedListener(textWatcher)
    return textWatcher
}