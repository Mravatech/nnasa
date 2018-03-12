package com.mnassa.widget

import android.content.Context
import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import com.mnassa.R
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.showKeyboard
import kotlinx.android.synthetic.main.controller_invite.view.*
import kotlinx.android.synthetic.main.item_pin.view.*

/**
 * Created by Peter on 07.03.2018.
 */
class PinView : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val digitsCount: Int
    private val filterInputType: InputFilter
    private val editTexts = ArrayList<EditText>()
    private val editTextBackgrounds = ArrayList<View>()
    //
    var onPinEnteredListener = { pin: String -> }
    var pinEnteringProgressListener = { progress: Float -> }

    init {
        orientation = HORIZONTAL
        digitsCount = resources.getInteger(R.integer.validation_code_length)

        val inflater = LayoutInflater.from(context)

        for (i in 0 until digitsCount) {
            val view = inflater.inflate(R.layout.item_pin, null)
            editTexts += view.etPinItem
            editTextBackgrounds += view.vPinBackground

            val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
            lp.weight = 1f
            view.layoutParams = lp

            addView(view)
        }

        filterInputType = InputFilter { source, start, end, _, _, _ ->
            // Only keep characters that are alphanumeric
            val builder = StringBuilder()
            for (i in start until end) {
                val c = source[i]
                if (Character.isDigit(c)) {
                    builder.append(c)
                }
            }

            // If all characters are valid, return null, otherwise only return the filtered characters
            val allCharactersValid = builder.length == end - start
            if (allCharactersValid) null else builder.toString()
        }

        editTexts.forEachIndexed { index, editText ->
            val lengthFilter = object : InputFilter.LengthFilter(1) {
                override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
                    val charSequence: CharSequence? = super.filter(source, start, end, dest, dstart, dend)
                    if (digitsCount > 0 && source != null && source.length > 1) {
                        pasteText(index, source)
                        return charSequence
                    }
                    if (charSequence.isNullOrEmpty() && !source.isNullOrEmpty() && dest.isNotEmpty() && dstart == 0) {
                        editText.post { editText.setText(source) }
                    } else if (dstart > 0) {
                        editText.post { goNext(index, source) }

                    }
                    return charSequence
                }
            }
            editText.filters = arrayOf(InputFilter.AllCaps(), lengthFilter, filterInputType)

            editText.setOnKeyListener { _, _, event ->
                if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DEL) {
                    if (editText.length() == 0 || editText.selectionStart == 0) {
                        goPrevious(index)
                    }
                }
                false
            }

            editText.addTextChangedListener(SimpleTextWatcher { s ->
                if (s.isNotEmpty()) {
                    editText.gravity = Gravity.CENTER_HORIZONTAL
                    goNext(index, null)
                } else {
                    editText.gravity = Gravity.START
                }

                editTextBackgrounds[index].visibility = if (s.isNotEmpty()) View.INVISIBLE else View.VISIBLE

                checkPinEntered()
            })

            editText.setTextIsSelectable(false)
            editText.customSelectionActionModeCallback = object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                    menu.clear()
                    menu.add(0, android.R.id.paste, 0, "Paste")
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false
                override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = false
                override fun onDestroyActionMode(mode: ActionMode) = Unit
            }
        }

        showKeyboard(editTexts.first())
    }

    protected fun pasteText(startIndex: Int, source: CharSequence) {
        if (startIndex < 0 || startIndex > digitsCount || startIndex >= editTexts.size) return
        post {
            var i = startIndex
            var indexForText = 0
            while (indexForText < source.length && i < editTexts.size) {
                editTexts[i].setText(source[indexForText].toString())
                val isLast = i == digitsCount - 1 || indexForText == source.length - 1
                if (isLast) {
                    editTexts[i].requestFocus()
                    editTexts[i].setSelection(editTexts[i].text.length)
                }
                i++
                indexForText++
            }
        }
    }


    protected fun goNext(index: Int, charSequence: CharSequence?) {
        if (index >= editTexts.size - 1 || editTexts.isEmpty()) {
            //last element
            return
        }
        val editText = editTexts[index + 1]
        if (!TextUtils.isEmpty(charSequence)) {
            editText.setText(charSequence)
        }
        editText.requestFocus()
        editText.setSelection(editText.text.length)
    }

    protected fun goPrevious(index: Int) {
        if (index == 0 || editTexts.isEmpty()) {
            //first element
            return
        }
        val editText = editTexts[index - 1]
        editText.requestFocus()
        editText.text = null
    }


    fun getPin(): String? {
        if (!editTexts.isEmpty()) {
            val stringBuilder = StringBuilder(editTexts.size)
            for (i in editTexts.indices) {
                if (editTexts[i].length() == 0) {
                    return null
                }
                stringBuilder.append(editTexts[i].text.toString())
            }
            return stringBuilder.toString()
        }
        return null
    }


    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        for (editText in editTexts) {
            editText.isEnabled = enabled
            if (!enabled) {
                editText.text = null
                editText.clearFocus()
            }
        }
        if (enabled && !editTexts.isEmpty()) {
            editTexts[0].requestFocus()
        }
    }

    protected fun checkPinEntered() {
        val pin = editTexts.joinToString(separator = "") { it.text.toString() }
        if (pin.length == digitsCount) {
            onPinEnteredListener(pin)
        }

        val lastNotEmptyEditTextIndex = editTexts.indexOfLast { it.text.isNotEmpty() }
        val progress = (lastNotEmptyEditTextIndex + 1).toFloat() / editTexts.size.toFloat()
        pinEnteringProgressListener(progress)
    }

}