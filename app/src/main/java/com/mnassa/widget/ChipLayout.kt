package com.mnassa.widget

import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.FloatRange
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.LongSparseArray
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.ListPopupWindow
import android.widget.TextView
import androidx.util.isEmpty
import androidx.util.valueIterator
import com.mnassa.R
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.impl.TagModelImpl
import com.mnassa.extensions.SimpleTextWatcher
import kotlinx.android.synthetic.main.chip_layout.view.*
import timber.log.Timber
import androidx.view.get

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
    private val chips = LongSparseArray<TagModel>()
    lateinit var chipSearch: ChipsAdapter.ChipSearch

    init {
        View.inflate(context, R.layout.chip_layout, this)
        etChipInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (etChipInput.text.toString().length >= MIN_SYMBOLS_TO_ADD_TAGS)
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
                focusOnView()
            } else {
                listPopupWindow.dismiss()
                focusLeftView()
            }
        }
        flChipContainer.setOnClickListener {
            etChipInput.requestFocus()
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(etChipInput, InputMethodManager.SHOW_IMPLICIT)
        }
        val observer = tvChipHeader.viewTreeObserver
        observer.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                tvChipHeader.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val transition = etChipInput.height.toFloat() + resources.getDimension(R.dimen.chip_et_margin_vertical)
                animateViews(ANIMATION_EDIT_TEXT_SCALE_HIDE, transition, INIT_DURATION, INIT_DURATION, ANIMATION_TEXT_VIEW_SCALE_BIG)
            }
        })
    }

    override fun onChipClick(tagModel: TagModel) {
        addChip(tagModel)
    }

    override fun onViewRemoved(key: Long) {
        chips.remove(key)
        Timber.i(chips.toString())
        if (!etChipInput.isFocused) {
            focusLeftView()
        }
    }

    override fun onEmptySearchResult() {
        listPopupWindow.dismiss()
    }


    fun getTags(): List<TagModel> {
        if (chips.isEmpty()) return emptyList()
        val tags = ArrayList<TagModel>(chips.size())
        for (tag in chips.valueIterator()){
            tags.add(tag)
        }
        return tags
    }

    fun setTags(tags: List<TagModel>) {
        chips.clear()
        val viewsToRemove = ArrayList<ChipView>()
        for (i in 0 until flChipContainer.childCount) {
            val view = flChipContainer[i]
            if (view is ChipView) {
                viewsToRemove.add(view)
            }
        }
        viewsToRemove.forEach {
            flChipContainer.removeView(it)
        }
        tags.forEach { addChip(it) }
    }

    private fun focusLeftView() {
        if (etChipInput.text.toString().isEmpty() && chips.isEmpty()) {
            val transition = etChipInput.height.toFloat() + resources.getDimension(R.dimen.chip_et_margin_vertical)
            animateViews(ANIMATION_EDIT_TEXT_SCALE_HIDE, transition, ANIMATION_DURATION, EDIT_TEXT_SHOW_HIDE, ANIMATION_TEXT_VIEW_SCALE_BIG)
        }
        colorViews(tvChipHeader, vChipBottomLine, R.color.chipDefaultEditTextHintColor)
    }

    private fun focusOnView() {
        val transition = -etChipInput.height.toFloat() + resources.getDimension(R.dimen.chip_et_margin_vertical) * MARGIN_COUNT
        animateViews(ANIMATION_EDIT_TEXT_SCALE_SHOW, transition, ANIMATION_DURATION, EDIT_TEXT_SHOW_DURATION, ANIMATION_TEXT_VIEW_SCALE_SMALL)
        colorViews(tvChipHeader, vChipBottomLine, R.color.colorAccent)
    }

    private fun addChip(value: TagModel?) {
        val tagModelTemp = if (value != null) {
            value
        } else {
            if (etChipInput.text.toString().isBlank()) {
                etChipInput.text = null
                return
            }
            TagModelImpl(null, etChipInput.text.toString(), null)
        }
        val key = System.currentTimeMillis()
        val position = flChipContainer.childCount - EDIT_TEXT_RESERVE
        val chipView = createChipView(tagModelTemp, key)
        flChipContainer.addView(chipView, position)
        chips.append(key, tagModelTemp)
        etChipInput.text = null
    }


    private fun animateViews(@FloatRange(from = 0.0, to = 1.0) scale: Float,
                             transition: Float,
                             timeTV: Long,
                             timeET: Long,
                             @FloatRange(from = 0.0, to = 1.0) scaleTV: Float) {
        etChipInput.animate().setDuration(timeET).scaleX(scale)
        etChipInput.animate().setDuration(timeET).scaleY(scale)
        val transit = tvChipHeader.width / HALF_VIEW_WIDTH * scaleTV - tvChipHeader.width / HALF_VIEW_WIDTH
        tvChipHeader.animate().setDuration(TV_HEADER_TRANSITION_X_DURATION).translationX(transit)
        tvChipHeader.animate().setDuration(timeTV).translationY(transition)
        tvChipHeader.animate().setDuration(timeTV).scaleX(scaleTV)
        tvChipHeader.animate().setDuration(timeTV).scaleY(scaleTV)
    }

    private fun colorViews(tvText: TextView, vBottomDivider: View, @ColorRes color: Int) {
        val colorRes = ContextCompat.getColor(context, color)
        tvText.setTextColor(colorRes)
        vBottomDivider.setBackgroundColor(colorRes)
    }

    private fun createChipView(tagModel: TagModel, position: Long): ChipView {
        val chipView = ChipView(context, tagModel, position, this)
        val params = FlowLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        chipView.layoutParams = params
        return chipView
    }

    private fun removeLastChip() {
        if (etChipInput.text.toString().isEmpty() &&
                (flChipContainer.childCount > EDIT_TEXT_RESERVE)) {
            getLastChipView().removeViewFromParent()
        }
    }

    private fun getLastChipView(): ChipView {
        return flChipContainer.getChildAt(flChipContainer.childCount - PRE_LAST_VIEW_IN_FLOW_LAYOUT) as ChipView
    }

    private fun showPopup() {
        if (!this::listPopupWindow.isInitialized) {
            adapter = ChipsAdapter(context, this, chipSearch)
            listPopupWindow = ListPopupWindow(context)
            listPopupWindow.setAdapter(adapter)
            listPopupWindow.anchorView = etChipInput
            listPopupWindow.height = ListPopupWindow.WRAP_CONTENT
            listPopupWindow.width = this.width - context.resources.getDimension(R.dimen.padding_horizontal).toInt()
            listPopupWindow.isModal = false
            listPopupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            listPopupWindow.promptPosition = ListPopupWindow.POSITION_PROMPT_BELOW
            etChipInput.addTextChangedListener(SimpleTextWatcher {
                if (it.isNotEmpty() && it.length >= MIN_SYMBOLS_TO_START_SEARCH) {
                    if (!listPopupWindow.isShowing) {
                        listPopupWindow.show()
                    }
                    adapter.search(it)
                } else {
                    listPopupWindow.dismiss()
                }
            })
        }
    }

    companion object {
        private const val EDIT_TEXT_RESERVE = 1
        private const val HALF_VIEW_WIDTH = 2
        private const val PRE_LAST_VIEW_IN_FLOW_LAYOUT = 2
        private const val MIN_SYMBOLS_TO_START_SEARCH = 3
        private const val MIN_SYMBOLS_TO_ADD_TAGS = 3
        private const val MARGIN_COUNT = 3
        private const val ANIMATION_DURATION = 200L
        private const val ANIMATION_EDIT_TEXT_SCALE_SHOW = 1f
        private const val ANIMATION_EDIT_TEXT_SCALE_HIDE = 0f
        private const val ANIMATION_TEXT_VIEW_SCALE_BIG = 1f
        private const val ANIMATION_TEXT_VIEW_SCALE_SMALL = 0.75f
        private const val EDIT_TEXT_SHOW_DURATION = 300L
        private const val EDIT_TEXT_SHOW_HIDE = 100L
        private const val TV_HEADER_TRANSITION_X_DURATION = 50L
        private const val INIT_DURATION = 0L
    }

}