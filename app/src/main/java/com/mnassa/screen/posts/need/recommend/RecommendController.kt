package com.mnassa.screen.posts.need.recommend

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.recommend.adapter.AccountsToRecommendRVAdapter
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
    private val adapter = AccountsToRecommendRVAdapter(bestMatchesAccounts)
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
            rvAccountsToRecommend.adapter = adapter

            btnRecommend.text = fromDictionary(R.string.posts_recommend_button)
            adapter.onSelectedAccountsChangedListener = {
                btnRecommend.isEnabled = it.isNotEmpty()
                btnRecommend.text = fromDictionary(R.string.posts_recommend_button) + " (${it.size})"
            }
            btnRecommend.isEnabled = false

            btnRecommend.setOnClickListener {
                resultListener.selectedAccounts = adapter.selectedAccounts.toList()
                close()
            }

            tvRecommendHeader.text = fromDictionary(R.string.posts_recommend_subtitle)
        }

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.connectionsChannel.consumeEach {
                adapter.isLoadingEnabled = false
                adapter.setAccounts(it)
                view.rlEmptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.INVISIBLE
                view.btnRecommend.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    interface OnRecommendPostResult {
        var selectedAccounts: List<ShortAccountModel>
    }

    companion object {
        private const val EXTRA_BEST_MATCHES = "EXTRA_BEST_MATCHES"
        private const val EXTRA_RECOMMEND_TO_PERSON_NAME = "EXTRA_RECOMMEND_TO_PERSON_NAME"

        fun newInstance(recommendToPersonName: String, bestMatchesAccounts: List<String> = emptyList()): RecommendController {
            val args = Bundle()
            args.putStringArrayList(EXTRA_BEST_MATCHES, ArrayList(bestMatchesAccounts))
            args.putString(EXTRA_RECOMMEND_TO_PERSON_NAME, recommendToPersonName)
            return RecommendController(args)
        }
    }
}