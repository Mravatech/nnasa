package com.mnassa.screen.posts.need.recommend

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import org.kodein.di.generic.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.recommend.adapter.AccountsToRecommendRVAdapter
import com.mnassa.screen.posts.need.recommend.adapter.SelectedAccountRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_post_recommend.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/27/2018.
 */
class RecommendController(args: Bundle) : MnassaControllerImpl<RecommendViewModel>(args) {
    override val layoutId: Int = R.layout.controller_post_recommend
    private val bestMatchesAccounts: List<String> by lazy { args.getStringArrayList(EXTRA_BEST_MATCHES) }
    override val viewModel: RecommendViewModel by instance()
    private val allAccountsAdapter = AccountsToRecommendRVAdapter(bestMatchesAccounts)
    private val selectedAccountsAdapter = SelectedAccountRVAdapter()
    private val resultListener by lazy { targetController as OnRecommendPostResult }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        require(targetController is OnRecommendPostResult) {
            "$targetController must implement ${OnRecommendPostResult::class.java.name}"
        }

        with(view) {
            toolbar.title = fromDictionary(R.string.posts_recommend_title)

            rvAccountsToRecommend.layoutManager = LinearLayoutManager(context)
            rvAccountsToRecommend.adapter = allAccountsAdapter

            rvSelectedAccounts.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            rvSelectedAccounts.adapter = selectedAccountsAdapter

            btnRecommend.text = fromDictionary(R.string.posts_recommend_button)
            allAccountsAdapter.onSelectedAccountsChangedListener = {
                btnRecommend.isEnabled = it.isNotEmpty()
                btnRecommend.text = fromDictionary(R.string.posts_recommend_button) + " (${it.size})"
                //
                selectedAccountsAdapter.set(it)
                rvSelectedAccounts.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE
            }
            selectedAccountsAdapter.onDataSourceChangedListener = {
                allAccountsAdapter.selectedAccounts = it.toSet()
            }
            btnRecommend.isEnabled = false
            rvSelectedAccounts.visibility = if (selectedAccountsAdapter.dataStorage.size == 0) View.GONE else View.VISIBLE

            btnRecommend.setOnClickListener {
                resultListener.recommendedAccounts = allAccountsAdapter.selectedAccounts.toList()
                close()
            }

            tvRecommendHeader.text = fromDictionary(R.string.posts_recommend_subtitle)
        }

        allAccountsAdapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.connectionsChannel.consumeEach {
                allAccountsAdapter.setAccounts(it)
                allAccountsAdapter.isLoadingEnabled = false
                view.rlEmptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.INVISIBLE
                view.btnRecommend.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE

                if (args.containsKey(EXTRA_SELECTED_ACCOUNTS)) {
                    allAccountsAdapter.selectedAccounts = args.getStringArrayList(EXTRA_SELECTED_ACCOUNTS).mapNotNull { id -> it.firstOrNull { it.id == id } }.toSet()
                    args.remove(EXTRA_SELECTED_ACCOUNTS)
                }
            }
        }
    }

    override fun onDestroyView(view: View) {
        allAccountsAdapter.destroyCallbacks()
        selectedAccountsAdapter.destroyCallbacks()
        super.onDestroyView(view)
    }

    interface OnRecommendPostResult {
        var recommendedAccounts: List<ShortAccountModel>
    }

    companion object {
        private const val EXTRA_BEST_MATCHES = "EXTRA_BEST_MATCHES"
        private const val EXTRA_RECOMMEND_TO_PERSON_NAME = "EXTRA_RECOMMEND_TO_PERSON_NAME"
        private const val EXTRA_SELECTED_ACCOUNTS = "EXTRA_SELECTED_ACCOUNTS"

        fun newInstance(
            recommendToPersonName: String,
            bestMatchesAccounts: List<String> = emptyList(),
            selectedAccounts: List<String> = emptyList()
        ): RecommendController {

            val args = Bundle()
            args.putStringArrayList(EXTRA_BEST_MATCHES, ArrayList(bestMatchesAccounts))
            args.putString(EXTRA_RECOMMEND_TO_PERSON_NAME, recommendToPersonName)
            args.putStringArrayList(EXTRA_SELECTED_ACCOUNTS, ArrayList(selectedAccounts))
            return RecommendController(args)
        }
    }
}