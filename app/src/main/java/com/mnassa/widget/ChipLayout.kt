package com.mnassa.widget

import android.content.Context
import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListPopupWindow
import android.widget.TextView
import androidx.view.get
import androidx.view.iterator
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
import kotlinx.coroutines.experimental.delay
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/12/2018
 */

class ChipLayout : LinearLayout, ChipsAdapter.ChipListener, KodeinAware {

    override val kodein: Kodein by closestKodein(context)

    private val languageProvider: LanguageProvider by instance()

    private val tagInteractor: TagInteractor by instance()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val allAvailableTags: Deferred<List<TagModel>>
    var onChipsChangeListener = { }

    private var searchJob: Job? = null

    private val searchPopupAdapter = ChipsAdapter(context, this)

    private val searchPopup by lazy {
        ListPopupWindow(context).apply {
            setAdapter(searchPopupAdapter)
            
            height = context.resources.getDimension(R.dimen.chip_popup_height).toInt()
            width = ViewGroup.LayoutParams.MATCH_PARENT

            isModal = false
            anchorView = etChipInput
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            promptPosition = ListPopupWindow.POSITION_PROMPT_BELOW
        }
    }

    init {
        allAvailableTags = async { tagInteractor.getAll() }

        View.inflate(context, R.layout.chip_layout, this)

        initInput()

        flChipContainer.setOnClickListener {
            etChipInput.requestFocus()
            val inputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(etChipInput, InputMethodManager.SHOW_IMPLICIT)
        }
        tvChipHeader.text = fromDictionary(R.string.need_create_tags_hint)
    }

    private fun initInput() {
        etChipInput.apply {
            hint = fromDictionary(R.string.reg_person_type_here)
            // Add tags by clicking the
            // action button on a keyboard.
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        val text = etChipInput.text.toString().trim()
                        if (text.length < MIN_SYMBOLS_TO_ADD_TAGS) {
                            etChipInput.setText(text) // apply the trim
                        } else {
                            etChipInput.text = null
                            addTag(text.asTag())
                            onChipsChangeListener()
                        }
                    }
                    else -> return@setOnEditorActionListener false
                }
                return@setOnEditorActionListener true
            }
            // Remove tags by clicking the
            // delete button on a keyboard.
            setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_DEL) {
                    removeTagAtEnd()
                    onChipsChangeListener()

                    true
                } else {
                    false
                }
            }
            addTextChangedListener(SimpleTextWatcher {
                val text = etChipInput.text.toString().trim()
                if (text.length >= MIN_SYMBOLS_TO_START_SEARCH) {
                    searchJob?.cancel()
                    searchJob = launchUI {
                        delay(SEARCH_DELAY_MS)
                        searchPopupAdapter.search(text)
                    }
                } else {
                    searchPopup.dismiss()
                }
            })
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    focusOnView()
                } else {
                    searchPopup.dismiss()
                    focusLeftView()
                }
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val tags = getTags().toList()
        val superState = super.onSaveInstanceState()
        return ChipLayoutState(tags, superState)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val superState = if (state is ChipLayoutState) {
            setTags(state.tags)
            state.superState
        } else {
            state
        }

        super.onRestoreInstanceState(superState)
    }

    /**
     * Called when user clicks on a tag in
     * the [searchPopup].
     */
    override fun onChipClick(tagModel: TagModel) {
        etChipInput.text = null
        addTag(tagModel)
    }

    override fun onSearchResult(text: String, tags: List<TagModel>) {
        val input = etChipInput.text.toString().trim()
        if (input != text) {
            return
        }

        if (tags.isEmpty()) {
            if (searchPopup.isShowing) {
                searchPopup.dismiss()
            }
        } else {
            searchPopup.show()
        }
    }

    override suspend fun getAllTags(): List<TagModel> = allAvailableTags.await()

    private fun removeTag(tag: TagModel) {
        val chipsSize = flChipContainer.childCount - EDIT_TEXT_RESERVE
        for (i in 0 until chipsSize) {
            val view = flChipContainer[i]
            if (view is ChipView && view.tagModel === tag) {
                removeTagAt(i)
                break
            }
        }
    }

    private fun removeTagAtEnd() {
        val index = (flChipContainer.childCount - 1) - EDIT_TEXT_RESERVE
        if (index >= 0) {
            removeTagAt(index)
        }
    }

    private fun removeTagAt(index: Int) {
        flChipContainer.removeViewAt(index)
    }

    private fun suggestTags(suggestedTags: List<AutoTagModelImpl>) {
        // Remove all previously suggested tags
        val chipsSize = flChipContainer.childCount - EDIT_TEXT_RESERVE
        for (i in (0 until chipsSize).reversed()) {
            val view = flChipContainer[i]
            if (view is ChipView && view.tagModel is AutoTagModelImpl) {
                removeTagAt(i)
            }
        }

        // Filter duplicates
        val tags = getTags().toList()
        val tagsToAdd = suggestedTags
            .filter { suggestedTag ->
                tags.firstOrNull {
                    it.id == suggestedTag.id
                } == null
            }

        // Add new tags
        tagsToAdd.forEach(::addTag)
    }

    fun setTags(tags: List<TagModel>) {
        // Remove all previous tags from
        // the container.
        while (flChipContainer.childCount > EDIT_TEXT_RESERVE) {
            flChipContainer.removeViewAt(0)
        }

        addTags(tags)
    }

    private fun addTags(tags: List<TagModel>) = tags.forEach(::addTag)

    private fun addTag(tag: TagModel) {
        val chipView = ChipView(context, tag) {
            removeTag(it)
            onChipsChangeListener()
        }
        val chipPosition = flChipContainer.childCount - EDIT_TEXT_RESERVE
        val chipLp = FlowLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        flChipContainer.addView(chipView, chipPosition, chipLp)
    }

    /**
     * Creates a sequence of tags from the
     * views of chips.
     */
    fun getTagsSequence(): Sequence<TagModel> = flChipContainer
        .iterator()
        .asSequence()
        .mapNotNull { (it as? ChipView)?.tagModel }

    override fun getTags(): List<TagModel> =  getTagsSequence().toList()

    private fun String.asTag() = TagModelImpl(
        id = null,
        status = null,
        name = TranslatedWordModelImpl(languageProvider, this)
    )

    private var scanForTagsJob: Job? = null
    private val autoDetectTextWatcher = SimpleTextWatcher { text ->
        scanForTagsJob?.cancel()
        scanForTagsJob = launchUI {
            val tags = async { scanForTags(text) }
            suggestTags(tags.await())
            onChipsChangeListener()
        }
    }

    fun autodetectTagsFrom(editText: EditText) {
        editText.removeTextChangedListener(autoDetectTextWatcher)
        editText.addTextChangedListener(autoDetectTextWatcher)
    }

    private suspend fun scanForTags(text: String): List<AutoTagModelImpl> {
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

        return result.map { AutoTagModelImpl(status = it.status, name = it.name, id = it.id) }
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

    private fun colorViews(tvText: TextView, vBottomDivider: View, @ColorRes color: Int) {
        val colorRes = ContextCompat.getColor(context, color)
        tvText.setTextColor(colorRes)
        vBottomDivider.setBackgroundColor(colorRes)
    }

    @Parcelize
    private class ChipLayoutState(
        val tags: List<TagModel>,
        val superState: Parcelable?
    ) : Parcelable

    companion object {
        private const val EDIT_TEXT_RESERVE = 1
        private const val MIN_SYMBOLS_TO_START_SEARCH = 3
        private const val MIN_SYMBOLS_TO_ADD_TAGS = 3

        private const val SEARCH_DELAY_MS = 200L
    }
}