package com.mnassa.screen.posts.need.sharing

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.mnassa.App
import com.mnassa.R
import com.mnassa.activity.SearchActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.PostPrivacyType
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.formattedName
import com.mnassa.extensions.isGone
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.buildnetwork.BuildNetworkAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_sharing_options.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 3/21/2018.
 */
class SharingOptionsController(args: Bundle) : MnassaControllerImpl<SharingOptionsViewModel>(args) {
    override val layoutId: Int = R.layout.controller_sharing_options
    private val excludedAccounts by lazy { args.getStringArrayList(EXTRA_EXCLUDED_ACCOUNTS).toHashSet() }
    private val restrictShareReduction by lazy { args.getBoolean(EXTRA_RESTRICT_SHARE_REDUCTION) }
    private val notUnselectableAccounts by lazy { args.getStringArrayList(EXTRA_NOT_UNSELECTEBLE_ACCOUNTS).toSortedSet() }
    private val canBePromoted by lazy { args.getBoolean(EXTRA_CAN_BE_PROMOTED, false) }
    override val viewModel: SharingOptionsViewModel by instance(arg = SharingOptionsViewModel.SharingOptionsParams(excludedAccounts))
    private val resultListener by lazy { targetController as OnSharingOptionsResult }
    private val adapter = BuildNetworkAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        require(targetController is OnSharingOptionsResult) {
            "$targetController must implement ${OnSharingOptionsResult::class.java.name}"
        }

        if (args.containsKey(EXTRA_PREDEFINED_OPTIONS)) {
            setSelection(args.getSerializable(EXTRA_PREDEFINED_OPTIONS) as PostPrivacyOptions)
            args.remove(EXTRA_PREDEFINED_OPTIONS)
        }

        with(view) {
            toolbar.withActionButton(fromDictionary(R.string.sharing_options_button)) {
                resultListener.sharingOptions = getSelection()
                close()
            }
            tvPromotePostTitle.text = fromDictionary(R.string.sharing_options_promote_title)
            tvMyNewsFeed.text = fromDictionary(R.string.sharing_options_newsfeed_title)
            tvConnections.text = fromDictionary(R.string.sharing_options_connections_title)
            tvPromotePostTitle.text = fromDictionary(R.string.sharing_options_promote_title).format(args.getLong(EXTRA_PROMOTE_PRICE))
            rlPromotePostRoot.isGone = !canBePromoted

            var ignoreCheckedListener = false
            adapter.onSelectedAccountsChangedListener = {
                ignoreCheckedListener = true
                rbPromotePost.isChecked = false
                rbMyNewsFeed.isChecked = it.isEmpty()
                ignoreCheckedListener = false
            }
            ivSearch.setOnClickListener {
                startActivityForResult(SearchActivity.start(context, adapter.dataStorage.toList(), SearchActivity.SHARING_TYPE, adapter.selectedAccounts), SearchActivity.REQUEST_CODE_SEARCH)
            }

            rbPromotePost.setOnCheckedChangeListener { button, isChecked ->
                if (ignoreCheckedListener) return@setOnCheckedChangeListener
                if (isChecked) {
                    adapter.selectedAccounts = emptySet()
                }
                if (rbMyNewsFeed.isChecked == isChecked) {
                    rbMyNewsFeed.isChecked = !isChecked
                }
            }
            rbMyNewsFeed.setOnCheckedChangeListener { button, isChecked ->
                if (ignoreCheckedListener) return@setOnCheckedChangeListener
                if (isChecked) {
                    adapter.selectedAccounts = emptySet()
                }
                if (rbPromotePost.isChecked == isChecked) {
                    rbPromotePost.isChecked = !isChecked
                }
            }
            rlPromotePostRoot.setOnClickListener {
                if (!rbPromotePost.isChecked) {
                    rbPromotePost.isChecked = true
                }
            }
            rlMyNewsFeedRoot.setOnClickListener {
                if (!rbMyNewsFeed.isChecked) {
                    rbMyNewsFeed.isChecked = true
                }
            }

            rvAllConnections.layoutManager = LinearLayoutManager(context)
            rvAllConnections.adapter = adapter
        }

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.allConnections.consumeEach {
                adapter.isLoadingEnabled = false
                adapter.set(it.filter { !notUnselectableAccounts.contains(it.id) })
            }
        }
    }


    override fun onDestroyView(view: View) {
        adapter.destroyCallbacks()
        view.rvAllConnections.adapter = null
        super.onDestroyView(view)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != SearchActivity.REQUEST_CODE_SEARCH) return
        when (resultCode) {
            SearchActivity.SHARING_RESULT -> {
                data?.extras
                    ?.getStringArrayList(SearchActivity.EXTRA_LIST_CHECK_BOX_CONTAINER_ITEMS_RESULT)
                    ?.also { selectedIds ->
                        adapter.selectedAccounts = HashSet(selectedIds)
                    }
            }
        }
    }

    private fun getSelection(): PostPrivacyOptions {
        with(requireNotNull(view)) {
            return PostPrivacyOptions(
                    privacyType = when {
                        rbPromotePost.isChecked -> PostPrivacyType.WORLD()
                        rbMyNewsFeed.isChecked -> PostPrivacyType.PUBLIC()
                        else -> PostPrivacyType.PRIVATE()
                    },
                    privacyConnections = notUnselectableAccounts + adapter.selectedAccounts
            )
        }
    }

    private fun setSelection(options: PostPrivacyOptions) {
        if (options.privacyConnections.isEmpty() && options.privacyType is PostPrivacyType.PRIVATE) {
            //server side logic bug
            setSelection(PostPrivacyOptions.DEFAULT)
            return
        }

        with(view ?: return) {
            rbPromotePost.isChecked = options.privacyType is PostPrivacyType.WORLD
            rbMyNewsFeed.isChecked = options.privacyType is PostPrivacyType.PUBLIC
            val selectedAccounts = options.privacyConnections.toSet()
            adapter.selectedAccounts = selectedAccounts

            if (restrictShareReduction) {
                if (options.privacyType is PostPrivacyType.PUBLIC) {
                    rvAllConnections.isGone = true
                }
                if (options.privacyType is PostPrivacyType.WORLD) {
                    rvAllConnections.isGone = true
                    rlMyNewsFeedRoot.isGone = true
                    rbMyNewsFeed.isEnabled = false
                }
            }

            rlConnectionsHeader.visibility = rvAllConnections.visibility
        }
    }

    companion object {
        private const val EXTRA_PREDEFINED_OPTIONS = "EXTRA_PREDEFINED_OPTIONS"
        private const val EXTRA_EXCLUDED_ACCOUNTS = "EXTRA_EXCLUDED_ACCOUNTS"
        private const val EXTRA_RESTRICT_SHARE_REDUCTION = "EXTRA_RESTRICT_SHARE_REDUCTION"
        private const val EXTRA_NOT_UNSELECTEBLE_ACCOUNTS = "EXTRA_NOT_UNSELECTEBLE_ACCOUNTS"
        private const val EXTRA_CAN_BE_PROMOTED = "EXTRA_CAN_BE_PROMOTED"
        private const val EXTRA_PROMOTE_PRICE = "EXTRA_PROMOTE_PRICE"

        fun <T> newInstance(
                accountsToExclude: List<String>,
                options: PostPrivacyOptions = PostPrivacyOptions.DEFAULT,
                listener: T,
                restrictShareReduction: Boolean,
                canBePromoted: Boolean,
                promotePrice: Long): SharingOptionsController where T : OnSharingOptionsResult, T : Controller {
            val args = Bundle()
            args.putSerializable(EXTRA_PREDEFINED_OPTIONS, options)
            args.putStringArrayList(EXTRA_EXCLUDED_ACCOUNTS, accountsToExclude.toCollection(ArrayList()))
            args.putBoolean(EXTRA_RESTRICT_SHARE_REDUCTION, restrictShareReduction)
            if (restrictShareReduction) {
                args.putStringArrayList(EXTRA_NOT_UNSELECTEBLE_ACCOUNTS, options.privacyConnections.toCollection(ArrayList()))
            } else {
                args.putStringArrayList(EXTRA_NOT_UNSELECTEBLE_ACCOUNTS, ArrayList())
            }
            args.putBoolean(EXTRA_CAN_BE_PROMOTED, canBePromoted)
            args.putLong(EXTRA_PROMOTE_PRICE, promotePrice)

            val result = SharingOptionsController(args)
            result.targetController = listener
            return result
        }
    }

    interface OnSharingOptionsResult {
        var sharingOptions: PostPrivacyOptions
    }
}

private const val MAX_SHARE_TO_USERNAMES = 2
suspend fun PostPrivacyOptions.format(): CharSequence {
    val privacyType = privacyType
    return fromDictionary(R.string.need_create_share_to_prefix).format(
            when {
                privacyType is PostPrivacyType.WORLD -> fromDictionary(R.string.need_create_to_all_mnassa)
                privacyType is PostPrivacyType.PUBLIC -> fromDictionary(R.string.need_create_to_newsfeed)
                privacyType is PostPrivacyType.GROUP -> privacyType.group.formattedName
                privacyConnections.isNotEmpty() -> {
                    val userInteractor: UserProfileInteractor = App.context.getInstance()
                    val usernames = privacyConnections.take(MAX_SHARE_TO_USERNAMES).mapNotNull { userInteractor.getProfileById(it) }.joinToString { it.formattedName }
                    if (privacyConnections.size <= 2) {
                        usernames
                    } else {
                        val tail = fromDictionary(R.string.need_create_to_connections_other).format(privacyConnections.size - 2)
                        "$usernames $tail"
                    }
                }
                else -> fromDictionary(R.string.posts_sharing_value_centre)
            }
    )
}