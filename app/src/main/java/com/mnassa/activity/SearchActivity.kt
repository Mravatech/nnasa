package com.mnassa.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.mnassa.R
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.screen.buildnetwork.BuildNetworkAdapter
import com.mnassa.screen.chats.ChatListAdapter
import com.mnassa.screen.events.details.participants.EventParticipantItem
import com.mnassa.screen.events.details.participants.EventParticipantsRVAdapter
import com.mnassa.screen.events.details.participants.EventSelectParticipantsRVAdapter
import com.mnassa.screen.posts.need.recommend.adapter.AccountsToRecommendRVAdapter
import com.mnassa.screen.posts.need.recommend.adapter.GroupedAccount
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.activity_search.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 5/8/2018
 */

//TODO: rewrite search logic
@Deprecated(message = "Will be removed")
class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val type = intent.getIntExtra(EXTRA_LIST_TYPE, 0)
        btnDone.text = fromDictionary(R.string.search_done)
        etSearch.hint = fromDictionary(R.string.search_hint)
        rvSearch.layoutManager = LinearLayoutManager(this)
        when (type) {
            ALL_PARTICIPANT_TYPE -> allParticipant()
            SELECT_PARTICIPANT_TYPE -> selectParticipant()
            CHAT_TYPE -> chat()
            SHARING_TYPE -> sharing()
            GROUPED_ACCOUNT_TYPE -> recommend()
        }
    }

    private fun chat() {
        val items = intent.getSerializableExtra(EXTRA_LIST_ITEMS) as ArrayList<ChatRoomModel>
        val adapter = ChatListAdapter()
        adapter.set(items)
        etSearch.addTextChangedListener(SimpleTextWatcher {
            adapter.searchByName(it)
        })
        adapter.onItemClickListener = {
            setResult(CHAT_RESULT, intent.putExtra(EXTRA_ITEM_TO_OPEN_SCREEN_RESULT, it))
            finish()
        }
        rvSearch.adapter = adapter
        btnDone.setOnClickListener {
            finish()
        }
    }

    private fun recommend() {
        val items = intent.getSerializableExtra(EXTRA_LIST_ITEMS) as ArrayList<GroupedAccount>
        val checkBoxes = intent.getSerializableExtra(EXTRA_LIST_CHECK_BOX_CONTAINER_ITEMS) as ArrayList<ShortAccountModel>
        val adapter = AccountsToRecommendRVAdapter(intent.getSerializableExtra(EXTRA_BEST_MATCHES_ITEMS) as ArrayList<String>)
        adapter.selectedAccounts = HashSet(checkBoxes)
        adapter.set(items)
        etSearch.addTextChangedListener(SimpleTextWatcher {
            adapter.searchByName(it)
        })
        rvSearch.adapter = adapter
        btnDone.setOnClickListener {
            intent.putExtra(EXTRA_LIST_CHECK_BOX_CONTAINER_ITEMS_RESULT, ArrayList(adapter.selectedAccounts))
            setResult(GROUPED_ACCOUNT_RESULT, intent.putExtra(EXTRA_LIST_RESULT, items))
            finish()
        }
    }

    private fun sharing() {
        val items = intent.getSerializableExtra(EXTRA_LIST_ITEMS) as ArrayList<ShortAccountModel>
        val checkBoxes = intent.getSerializableExtra(EXTRA_LIST_CHECK_BOX_CONTAINER_ITEMS) as ArrayList<String>
        val adapter = BuildNetworkAdapter()
        adapter.selectedAccounts = HashSet(checkBoxes)
        adapter.set(items)
        etSearch.addTextChangedListener(SimpleTextWatcher {
            adapter.searchByName(it)
        })
        rvSearch.adapter = adapter
        btnDone.setOnClickListener {
            val intent = Intent().apply {
                val bundle = Bundle().apply {
                    val selectedIds = ArrayList(adapter.selectedAccounts)
                    putStringArrayList(EXTRA_LIST_CHECK_BOX_CONTAINER_ITEMS_RESULT, selectedIds)
                }
                putExtras(bundle)
            }

            setResult(SHARING_RESULT, intent)
            finish()
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
        const val REQUEST_CODE_SEARCH = 999

        private const val EXTRA_LIST_TYPE = "EXTRA_LIST_TYPE"
        private const val EXTRA_LIST_ITEMS = "EXTRA_LIST_ITEMS"
        private const val EXTRA_LIST_CHECK_BOX_CONTAINER_ITEMS = "EXTRA_LIST_CHECK_BOX_CONTAINER_ITEMS"
        private const val EXTRA_BEST_MATCHES_ITEMS = "EXTRA_BEST_MATCHES_ITEMS"
        const val EXTRA_LIST_RESULT = "EXTRA_LIST_RESULT"
        const val EXTRA_ITEM_TO_OPEN_SCREEN_RESULT = "EXTRA_ITEM_TO_OPEN_SCREEN_RESULT"
        const val EXTRA_LIST_CHECK_BOX_CONTAINER_ITEMS_RESULT = "EXTRA_LIST_CHECK_BOX_CONTAINER_ITEMS_RESULT"

        const val ALL_PARTICIPANT_RESULT = 101
        const val ALL_PARTICIPANT_TYPE = 1
        const val SELECT_PARTICIPANT_RESULT = 102
        const val SELECT_PARTICIPANT_TYPE = 2
        const val CHAT_RESULT = 103
        const val CHAT_TYPE = 3
        const val SHARING_RESULT = 104
        const val SHARING_TYPE = 4
        const val GROUPED_ACCOUNT_RESULT = 105
        const val GROUPED_ACCOUNT_TYPE = 5

        fun <ITEM, CHECK> start(context: Context, list: List<ITEM>, type: Int, checkBoxes: Set<CHECK>, bestMatches: List<String>? = null): Intent {
            val extraList: ArrayList<ITEM> = ArrayList(list)

            val intent = Intent(context, SearchActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_LIST_TYPE, type)
            bundle.putSerializable(EXTRA_LIST_ITEMS, extraList)
            checkBoxes.let {
                bundle.putSerializable(EXTRA_LIST_CHECK_BOX_CONTAINER_ITEMS, ArrayList(it))
            }
            bestMatches?.let {
                bundle.putSerializable(EXTRA_BEST_MATCHES_ITEMS, ArrayList(it))
            }
            intent.putExtras(bundle)
            return intent
        }

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