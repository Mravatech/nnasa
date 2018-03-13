package com.mnassa.widget

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.TagModelTemp
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class ChipsAdapter(
        context: Context,
        private val chipListener: ChipListener,
        private val chipSearch: ChipSearch)
    : ArrayAdapter<TagModelTemp>(context,
        android.R.layout.simple_expandable_list_item_1,
        android.R.id.text1) {

    private var resultList: List<TagModelTemp> = mutableListOf()

    override fun getCount() = resultList.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = super.getView(position, convertView, parent)
        val item = getItem(position)
        val textView1 = row.findViewById<TextView>(android.R.id.text1)
        textView1.text = item.name
        row.setOnClickListener { chipListener.onChipClick(item) }
        return row
    }

    override fun getItem(position: Int) = resultList[position]

    private var searchJob: Job? = null
    fun search(text: String) {
        searchJob?.cancel()
        searchJob = launch(UI) {
            delay(400)
            resultList = chipSearch.search(text)
            if (resultList.isEmpty()) {
                chipListener.onEmptySearchResult()
            }
            notifyDataSetChanged()
        }
    }

    interface ChipListener {
        fun onChipClick(tagModel: TagModelTemp)
        fun onEmptySearchResult()
    }

    interface ChipSearch {
        suspend fun search(search: String): List<TagModelTemp>
    }
}
