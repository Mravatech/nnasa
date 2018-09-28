package com.mnassa.widget

import android.content.Context
import android.os.Parcelable
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListPopupWindow
import android.widget.TextView
import com.mnassa.R
import com.mnassa.core.addons.launchUI
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.impl.AutoTagModelImpl
import com.mnassa.domain.model.impl.TagModelImpl
import com.mnassa.domain.model.impl.TranslatedWordModelImpl
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.translation.fromDictionary
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.util.LinkedHashSet
import kotlin.collections.ArrayList

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/12/2018
 */

class ChipLayout : LinearLayout, ChipView.OnChipListener, ChipsAdapter.ChipListener, KodeinAware {

    override val kodein: Kodein by closestKodein(context)

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private lateinit var listPopupWindow: ListPopupWindow
    private lateinit var adapter: ChipsAdapter
    private val allVisibleTags = LinkedHashSet<TagModel>()
    private val allAvailableTags: Deferred<List<TagModel>>
    private val languageProvider: LanguageProvider by instance()
    private val tagInteractor: TagInteractor by instance()
    var onChipsChangeListener = { }

    init {
        allAvailableTags = async { tagInteractor.getAll() }

        View.inflate(context, R.layout.chip_layout, this)
        etChipInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (etChipInput.text.toString().length >= MIN_SYMBOLS_TO_ADD_TAGS) {
                    if (etChipInput.text.toString().isBlank()) {
                        etChipInput.text = null
                        return@setOnEditorActionListener true
                    }
                    val tag = TagModelImpl(
                            status = null,
                            name = TranslatedWordModelImpl(languageProvider, etChipInput.text.toString()),
                            id = null,
                            localId = System.nanoTime()
                    )
                    addChip(tag)
                }
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
        etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
        tvChipHeader.text = fromDictionary(R.string.need_create_tags_hint)
    }

    override fun onSaveInstanceState(): Parcelable {
        return ChipLayoutState(getTags(), allVisibleTags.toList(), super.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is ChipLayoutState) {
            super.onRestoreInstanceState(state.superState)
            setTags(state.tags)
            allVisibleTags.clear()
            allVisibleTags.addAll(state.allVisibleTags)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onChipClick(tagModel: TagModel) {
        addChip(tagModel)
    }

    override fun onSearchResult(text: String, tags: List<TagModel>) {
        if (etChipInput.text.toString() != text) return

        if (tags.isEmpty() && listPopupWindow.isShowing) {
            listPopupWindow.dismiss()
        }
        if (tags.isNotEmpty()) {
            listPopupWindow.show()
        }
    }

    override fun getAlreadyAddedTags(): Set<TagModel> = allVisibleTags
    override suspend fun getAllTags(): List<TagModel> = allAvailableTags.await()

    override fun onViewRemoved(key: Long) {
        allVisibleTags.firstOrNull { it.localId == key }?.also { allVisibleTags.remove(it) }
        if (!etChipInput.isFocused) {
            focusLeftView()
        }
        onChipsChangeListener()
    }

    fun setTags(tags: List<TagModel>) {
        while (allVisibleTags.isNotEmpty()) {
            removeLastChip()
        }
        addTags(tags)
    }

    fun addTags(tags: List<TagModel>) {
        val observer = tvChipHeader.viewTreeObserver
        observer.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                tvChipHeader.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        tags.forEach { addChip(it) }
    }

    fun getTags(): List<TagModel> = allVisibleTags.toList()

    private var scanForTagsJob: Job? = null
    private val autoDetectTextWatcher = SimpleTextWatcher { text ->
        scanForTagsJob?.cancel()
        scanForTagsJob = launchUI {
            val tags = async { scanForTags(text) }
            addDetectedTags(tags.await())
        }
    }

    fun autodetectTagsFrom(editText: EditText) {
        editText.removeTextChangedListener(autoDetectTextWatcher)
        editText.addTextChangedListener(autoDetectTextWatcher)
    }

    private suspend fun scanForTags(text: String): List<TagModel> {
        val result = ArrayList<TagModel>()
        val words = text.toLowerCase().split(" ", ",", ".", ";").filter { it.isNotBlank() }.distinct()
        val searchPhrase = StringBuilder()

        for (i in 0 until words.size) {
            val word = words[i]
            searchPhrase.setLength(0)
            searchPhrase.append(word)

            var tags = findTagWhichStartsWith(searchPhrase.toString())
            if (tags.isEmpty()) continue
            result += tags.filter { it.isTheSame(searchPhrase.toString()) }

            for (j in (i + 1) until words.size) {
                val nextWord = words[j]
                searchPhrase.append(" ")
                searchPhrase.append(nextWord)

                tags = findTagWhichStartsWith(searchPhrase.toString())
                if (tags.isEmpty()) break
                result += tags.filter { it.isTheSame(searchPhrase.toString()) }
            }
        }

        return result.map { AutoTagModelImpl(status = it.status, name = it.name, id = it.id, localId = it.localId) }
    }

    private suspend fun removeAllDetectedTags() {
        val manualTags = allVisibleTags.filterNot { it is AutoTagModelImpl }
        setTags(manualTags.toList())
    }

    private suspend fun addDetectedTags(detectedTags: List<TagModel>) {
        removeAllDetectedTags()
        setTags(allVisibleTags.toList() + detectedTags)
    }

    private suspend fun findTagWhichStartsWith(prefix: String): List<TagModel> {
        return allAvailableTags.await().filter {
            it.name.toString().replace("#", "").startsWith(prefix, ignoreCase = true) ||
                    it.name.engTranslate?.replace("#", "")?.startsWith(prefix, ignoreCase = true) == true ||
                    it.name.arabicTranslate?.replace("#", "")?.startsWith(prefix, ignoreCase = true) == true
        }
    }

    private suspend fun TagModel.isTheSame(phrase: String): Boolean {
        return name.toString().replace("#", "").toLowerCase() == phrase.toLowerCase()
    }

    private fun focusLeftView() {
        colorViews(tvChipHeader, vChipBottomLine, R.color.chip_edit_text_hint)
    }

    private fun focusOnView() {
        colorViews(tvChipHeader, vChipBottomLine, R.color.accent)
    }

    private fun addChip(value: TagModel) {
        val position = flChipContainer.childCount - EDIT_TEXT_RESERVE
        val chipView = createChipView(value, value.localId)
        flChipContainer.addView(chipView, position)
        allVisibleTags.add(value)
        etChipInput.text = null
        onChipsChangeListener()
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
            adapter = ChipsAdapter(context, this)
            listPopupWindow = ListPopupWindow(context)
            listPopupWindow.setAdapter(adapter)
            listPopupWindow.anchorView = etChipInput
            listPopupWindow.height = ListPopupWindow.WRAP_CONTENT
            listPopupWindow.width = this.width - context.resources.getDimension(R.dimen.padding_horizontal).toInt()
            listPopupWindow.isModal = false
            listPopupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            listPopupWindow.promptPosition = ListPopupWindow.POSITION_PROMPT_BELOW
            etChipInput.addTextChangedListener(SimpleTextWatcher {
                if (it.isNotBlank() && it.length >= MIN_SYMBOLS_TO_START_SEARCH) {
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

    @Parcelize
    private class ChipLayoutState(val tags: List<TagModel>, val allVisibleTags: List<TagModel>, val superState: Parcelable?) : Parcelable

    companion object {
        private const val EDIT_TEXT_RESERVE = 1
        private const val PRE_LAST_VIEW_IN_FLOW_LAYOUT = 2
        private const val MIN_SYMBOLS_TO_START_SEARCH = 3
        private const val MIN_SYMBOLS_TO_ADD_TAGS = 3
    }
}