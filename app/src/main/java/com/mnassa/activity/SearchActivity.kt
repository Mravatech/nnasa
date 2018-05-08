package com.mnassa.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.mnassa.R
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.screen.chats.ChatListAdapter
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
        val listType = intent.getSerializableExtra(EXTRA_LIST_ITEMS) as ArrayList<ChatRoomModel>
        btnDone.text = fromDictionary(R.string.search_done)
        etSearch.hint = fromDictionary(R.string.search_hint)
        rvSearch.layoutManager = LinearLayoutManager(this)
        val adapter = ChatListAdapter()
        adapter.set(intent.getSerializableExtra(EXTRA_LIST_ITEMS) as ArrayList<ChatRoomModel>)
        rvSearch.adapter = adapter
        etSearch.addTextChangedListener(SimpleTextWatcher{
            adapter.searchByName(it)
        })
        //todo have to have it done
    }


    fun setAdapterByType() {
        //todo have to have it done
    }

    companion object {

        private const val EXTRA_LIST_TYPE = "EXTRA_LIST_TYPE"
        private const val EXTRA_LIST_ITEMS = "EXTRA_LIST_ITEMS"

        const val CONNECTION_RECOMINDATION = 1

        fun <ITEM> start(list: List<ITEM>, type: Int, context: Context) {
            val extraList: ArrayList<ITEM> = ArrayList(list)

            val intent = Intent(context, SearchActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_LIST_TYPE, type)
            bundle.putSerializable(EXTRA_LIST_ITEMS, extraList)

            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }

}