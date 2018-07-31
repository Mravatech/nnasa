package com.mnassa.widget

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.TagModel
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class ChipsAdapter(
        context: Context,
        private val chipListener: ChipListener)
    : ArrayAdapter<TagModel>(context,
        android.R.layout.simple_expandable_list_item_1,
        android.R.id.text1) {

    private var resultList: List<TagModel> = mutableListOf()

    override fun getCount() = resultList.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = super.getView(position, convertView, parent)
        val item = getItem(position)
        row.findViewById<TextView>(android.R.id.text1).text = item.name.toString()
        row.setOnClickListener { chipListener.onChipClick(item) }
        return row
    }

    override fun getItem(position: Int) = resultList[position]

    private var searchJob: Job? = null
    fun search(text: String) {
        searchJob?.cancel()

        val query = text.toLowerCase()
        val newList = resultList.filter { it.name.toString().toLowerCase().contains(query) }
        if (newList.isEmpty()) {
            chipListener.onEmptySearchResult()
        } else if (newList.size != resultList.size) {
            resultList = newList
            notifyDataSetChanged()
            if (newList.isNotEmpty()) return
        }

        searchJob = launch(UI) {
            delay(USER_STOP_TYPING)
            resultList = searchTags(text)
            if (resultList.isEmpty()) {
                chipListener.onEmptySearchResult()
            }
            notifyDataSetChanged()
        }
    }

    private suspend fun searchTags(text: String): List<TagModel> = context.getInstance<TagInteractor>().search(text)

    interface ChipListener {
        fun onChipClick(tagModel: TagModel)
        fun onEmptySearchResult()
    }

    companion object {
        private const val USER_STOP_TYPING = 400
    }
}
