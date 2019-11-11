package com.mnassa.screen.posts.need.recommend

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluelinelabs.conductor.Controller
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.invite.InviteController
import com.mnassa.screen.posts.need.recommend.adapter.AccountsToRecommendRVAdapter
import com.mnassa.screen.posts.need.recommend.adapter.CheckboxCount
import com.mnassa.screen.posts.need.recommend.adapter.SelectedAccountRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_post_recommend.*
import kotlinx.android.synthetic.main.controller_post_recommend.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 3/27/2018.
 */
class RecommendController(args: Bundle) : MnassaControllerImpl<RecommendViewModel>(args) {
    override val layoutId: Int = R.layout.controller_post_recommend
    private val bestMatchesAccounts: List<String> by lazy { args.getStringArrayList(EXTRA_BEST_MATCHES) }
    private val excludedAccounts: List<String> by lazy { args.getStringArrayList(EXTRA_EXCLUDED_ACCOUNTS) }
    override val viewModel: RecommendViewModel by instance(arg = RecommendViewModel.RecommendViewModelParams(excludedAccounts))
    private lateinit var allAccountsAdapter: AccountsToRecommendRVAdapter
    private val selectedAccountsAdapter = SelectedAccountRVAdapter()
    private val resultListener by lazy { targetController as OnRecommendPostResult }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        require(targetController is OnRecommendPostResult) {
            "$targetController must implement ${OnRecommendPostResult::class.java.name}"
        }

        with(view) {

            toolbar.title = "Recommend[0/20]"

            allAccountsAdapter = AccountsToRecommendRVAdapter(bestMatchesAccounts, checkboxCount = object : CheckboxCount {
                override fun checkBoxCount(sum: Int) {
                    toolbar.title = "Recommended[$sum/20]"
                }
            })



            toolbar.withActionButton(fromDictionary(R.string.posts_recommend_button)) {
                resultListener.onRecommendedAccountResult(allAccountsAdapter.selectedAccounts.toList())
                close()
            }
            toolbar.actionButtonClickable = !allAccountsAdapter.dataStorage.isEmpty()


            rvAccountsToRecommend.layoutManager = LinearLayoutManager(context)
            rvAccountsToRecommend.adapter = allAccountsAdapter

            rvSelectedAccounts.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            rvSelectedAccounts.adapter = selectedAccountsAdapter

            allAccountsAdapter.onSelectedAccountsChangedListener = {
                toolbar.actionButtonClickable = it.isNotEmpty()
                //
                selectedAccountsAdapter.set(it)
                rvSelectedAccounts.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE
            }
            allAccountsAdapter.onSearchClickListener = { onSearchClick(view) }
            selectedAccountsAdapter.onDataChangedListener = {
                allAccountsAdapter.selectedAccounts = selectedAccountsAdapter.dataStorage.toSet()
            }
            rvSelectedAccounts.visibility = if (selectedAccountsAdapter.dataStorage.size == 0) View.GONE else View.VISIBLE

            tvRecommendHeader.text = fromDictionary(R.string.posts_recommend_subtitle)

            btnInvite.text = fromDictionary(R.string.invite_new_connection)
            btnInvite.setOnClickListener { open(InviteController.newInstance()) }

        }

        allAccountsAdapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.connectionsChannel.consumeEach {



                allAccountsAdapter.setAccounts(it)
                Log.d("people", "${it.size}")
                allAccountsAdapter.isLoadingEnabled = false
                view.rlEmptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.INVISIBLE

                if (args.containsKey(EXTRA_SELECTED_ACCOUNTS)) {
                    allAccountsAdapter.selectedAccounts = args.getStringArrayList(EXTRA_SELECTED_ACCOUNTS).mapNotNull { id -> it.firstOrNull { it.id == id } }.toSet()
                    args.remove(EXTRA_SELECTED_ACCOUNTS)
                }
            }
        }
    }

    interface People{
        fun total(all: Int)
    }

    override fun onDestroyView(view: View) {
        allAccountsAdapter.destroyCallbacks()
        view.rvAccountsToRecommend.adapter = null
        view.rvSelectedAccounts.adapter = null
        super.onDestroyView(view)
    }

    private fun onSearchClick(view: View) {
        view.toolbar.startSearch(
                onSearchCriteriaChanged = allAccountsAdapter::searchByName,
                onSearchDone = {}
        )
    }

    interface OnRecommendPostResult {
        fun onRecommendedAccountResult(recommendedAccounts: List<ShortAccountModel>)
    }

    companion object {
        private const val EXTRA_BEST_MATCHES = "EXTRA_BEST_MATCHES"
        private const val EXTRA_SELECTED_ACCOUNTS = "EXTRA_SELECTED_ACCOUNTS"
        private const val EXTRA_EXCLUDED_ACCOUNTS = "EXTRA_EXCLUDED_ACCOUNTS"

        fun <T> newInstance(
                bestMatchesAccounts: List<String> = emptyList(),
                selectedAccounts: List<String> = emptyList(),
                excludedAccounts: List<String> = emptyList(),
                listener: T
        ): RecommendController where T : OnRecommendPostResult, T : Controller {

            val args = Bundle()
            args.putStringArrayList(EXTRA_BEST_MATCHES, ArrayList(bestMatchesAccounts))
            args.putStringArrayList(EXTRA_SELECTED_ACCOUNTS, ArrayList(selectedAccounts))
            args.putStringArrayList(EXTRA_EXCLUDED_ACCOUNTS, ArrayList(excludedAccounts))
            val controller = RecommendController(args)
            controller.targetController = listener
            return controller
        }
    }
}