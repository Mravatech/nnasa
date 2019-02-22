package com.mnassa.widget

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.model.TagModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ChipsAdapter(
        context: Context,
        private val chipListener: ChipListener)
    : ArrayAdapter<TagModel>(context,
        android.R.layout.simple_expandable_list_item_1,
        android.R.id.text1) {

    private var resultList: List<TagModel> = mutableListOf()
    private var lastSearchText = ""

    override fun getCount() = resultList.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = super.getView(position, convertView, parent)
        val item = getItem(position)
        row.findViewById<TextView>(android.R.id.text1).text = formatText(item, lastSearchText)
        row.setOnClickListener { chipListener.onChipClick(item) }
        return row
    }

    override fun getItem(position: Int) = resultList[position]

    private fun formatText(tagModel: TagModel, textToUnderline: String): CharSequence {
        val span = SpannableString(tagModel.name.toString())

        var startIndex = 0
        while (true) {
            startIndex = span.indexOf(textToUnderline, startIndex, true).takeIf { it >= 0 } ?: break
            span.setSpan(StyleSpan(Typeface.BOLD), startIndex, startIndex + textToUnderline.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            startIndex += textToUnderline.length
        }

        return span
    }

    private var searchJob: Job? = null
    private val searchMutex = Mutex()
    fun search(text: String) {
        lastSearchText = text
        searchJob?.cancel()

        searchJob = GlobalScope.launchWorker(CoroutineStart.UNDISPATCHED) {
            searchMutex.withLock {
                setResult(text, chipListener.getAllTags().filterLowercase(text))
            }
        }
    }

    private fun List<TagModel>.filterLowercase(text: String): List<TagModel> {
        if (text.isNullOrBlank()) return emptyList()
        val text = text.toLowerCase()
        return filter { it.name.toString().toLowerCase().contains(text) }
    }

    private fun setResult(text: String, result: List<TagModel>) {
        //exclude already added tags
        val alreadyAddedTags = chipListener.getTags()
        val result = result.filterNot { tagToAdd ->
            val tagToAddText = tagToAdd.name.toString().toLowerCase()
            alreadyAddedTags.any { addedTag ->
                (tagToAdd.id == addedTag.id && tagToAdd.id != null) ||
                        (tagToAddText == addedTag.name.toString().toLowerCase())
            }
        }

        resultList = result
        notifyDataSetChanged()
        chipListener.onSearchResult(text, result)
    }

    interface ChipListener {
        fun onChipClick(tagModel: TagModel)
        fun onSearchResult(text: String, tags: List<TagModel>)
        fun getTags(): List<TagModel>
        suspend fun getAllTags(): List<TagModel>
    }

}
