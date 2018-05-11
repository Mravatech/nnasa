package com.mnassa.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.mnassa.R
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.screen.events.details.participants.EventParticipantItem
import com.mnassa.screen.events.details.participants.EventParticipantsRVAdapter
import com.mnassa.screen.events.details.participants.EventSelectParticipantsRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.activity_search.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 5/8/2018
 */

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val type = intent.getIntExtra(EXTRA_LIST_TYPE, 0)
        btnDone.text = fromDictionary(R.string.search_done)
        etSearch.hint = fromDictionary(R.string.search_hint)
        rvSearch.layoutManager = LinearLayoutManager(this)
        when (type) {
            ALL_PARTICIPANT -> allParticipant()
            SELECT_PARTICIPANT -> selectParticipant()
        }
    }

    private fun selectParticipant() {
        val items = intent.getSerializableExtra(EXTRA_LIST_ITEMS) as ArrayList<EventParticipantItem>
        val adapter = EventSelectParticipantsRVAdapter()
        adapter.set(items)
        etSearch.addTextChangedListener(SimpleTextWatcher {
            adapter.searchByName(it)
        })
        rvSearch.adapter = adapter
        btnDone.setOnClickListener {
            setResult(SELECT_PARTICIPANT_RESULT, intent.putExtra(EXTRA_LIST_RESULT, items))
            finish()
        }
    }

    private fun allParticipant() {
        val items = intent.getSerializableExtra(EXTRA_LIST_ITEMS) as ArrayList<EventParticipantItem>
        val adapter = EventParticipantsRVAdapter()
        adapter.set(items)
        adapter.onParticipantClickListener = {
            setResult(ALL_PARTICIPANT_RESULT, intent.putExtra(EXTRA_ITEM_TO_OPEN_SCREEN_RESULT, it))
            finish()
        }
        etSearch.addTextChangedListener(SimpleTextWatcher {
            adapter.searchByName(it)
        })
        rvSearch.adapter = adapter
        btnDone.setOnClickListener {
            finish()
        }
    }

    companion object {

        private const val EXTRA_LIST_TYPE = "EXTRA_LIST_TYPE"
        private const val EXTRA_LIST_ITEMS = "EXTRA_LIST_ITEMS"
        const val EXTRA_LIST_RESULT = "EXTRA_LIST_RESULT"
        const val EXTRA_ITEM_TO_OPEN_SCREEN_RESULT = "EXTRA_ITEM_TO_OPEN_SCREEN_RESULT"

        const val ALL_PARTICIPANT_RESULT = 101
        const val SELECT_PARTICIPANT_RESULT = 102
        const val ALL_PARTICIPANT = 1
        const val SELECT_PARTICIPANT = 2

        fun <ITEM> start(context: Context, list: List<ITEM>, type: Int): Intent {
            val extraList: ArrayList<ITEM> = ArrayList(list)

            val intent = Intent(context, SearchActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_LIST_TYPE, type)
            bundle.putSerializable(EXTRA_LIST_ITEMS, extraList)

            intent.putExtras(bundle)
            return intent
        }
    }

}