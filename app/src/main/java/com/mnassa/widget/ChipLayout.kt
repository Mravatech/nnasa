package com.mnassa.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.LongSparseArray
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.ListPopupWindow
import com.mnassa.R
import com.mnassa.domain.model.TagModel
import com.mnassa.extensions.SimpleTextWatcher
import kotlinx.android.synthetic.main.chip_layout.view.*
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/12/2018
 */

class ChipLayout : LinearLayout, ChipView.OnChipListener, ChipsAdapter.ChipListener {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private lateinit var listPopupWindow: ListPopupWindow
    private lateinit var adapter: ChipsAdapter
    private val chips = LongSparseArray<TagModelTemp>()
    lateinit var chipSearch: ChipsAdapter.ChipSearch

    init {
        View.inflate(context, R.layout.chip_layout, this)
        etChipInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addChip(null)
            }
            true
        }
        etChipInput.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_DEL) {
                removeLastChip()
                return@OnKeyListener true
            }
            false
        })
        etChipInput.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showPopup()
            } else {
                listPopupWindow.dismiss()
            }
        }
    }

    override fun onChipClick(tagModel: TagModel) {
        addChip(TagModelTemp(tagModel.status, tagModel.nameAr, tagModel.nameEn, tagModel.id))
    }

    override fun onViewRemoved(key: Long) {
        chips.remove(key)
        Timber.i(chips.toString())
    }

    override fun onEmptySearchResult() {
        if (this::listPopupWindow.isInitialized)
            listPopupWindow.dismiss()
    }

    private fun addChip(value: TagModelTemp?) {
        val tagModelTemp: TagModelTemp = value
                ?: TagModelTemp(null, null, etChipInput.text.toString(), null)
        etChipInput.setText("")
        val key = System.currentTimeMillis()
        val position = flChipContainer.childCount - EDIT_TEXT_RESERVE
        val c = createChipView(tagModelTemp, key)
        flChipContainer.addView(c, position)
        chips.append(key, tagModelTemp)
        etChipInput.setText("")
    }

    private fun createChipView(tagModelTemp: TagModelTemp, position: Long): ChipView {
        val chipView = ChipView(context, tagModelTemp, position, this)
        val params = FlowLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params.rightMargin = 10
        chipView.layoutParams = params
        return chipView
    }

    private fun removeLastChip() {
        if (etChipInput.text.toString().isEmpty() and
                (flChipContainer.childCount > EDIT_TEXT_RESERVE)) {
            getLastChipView().removeViewFromParent()
        }
    }

    private fun getLastChipView(): ChipView {
        return flChipContainer.getChildAt(flChipContainer.childCount - 2) as ChipView
    }

    private fun showPopup() {
        if (!this::listPopupWindow.isInitialized) {
            adapter = ChipsAdapter(context, this, chipSearch)
            listPopupWindow = ListPopupWindow(context)
            listPopupWindow.setAdapter(adapter)
            listPopupWindow.anchorView = etChipInput
            listPopupWindow.height = ListPopupWindow.WRAP_CONTENT
            listPopupWindow.width = this.width
            listPopupWindow.isModal = false
            listPopupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            listPopupWindow.promptPosition = ListPopupWindow.POSITION_PROMPT_BELOW
            etChipInput.addTextChangedListener(SimpleTextWatcher {
                if (this::listPopupWindow.isInitialized) {
                    if (!TextUtils.isEmpty(it)) {
                        listPopupWindow.let {
                            if (!it.isShowing) {
                                listPopupWindow.show()
                            }
                        }
                        adapter.search(it)
                    } else {
                        listPopupWindow.dismiss()
                    }
                }
            })
        }
    }

    companion object {
        private const val EDIT_TEXT_RESERVE = 1
    }

}