package com.mnassa.screen.posts.need.sharing

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.mnassa.App
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.PostPrivacyType
import com.mnassa.extensions.isGone
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.buildnetwork.BuildNetworkAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_sharing_options.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance
import java.io.Serializable

/**
 * Created by Peter on 3/21/2018.
 */
class SharingOptionsController(args: Bundle) : MnassaControllerImpl<SharingOptionsViewModel>(args) {
    override val layoutId: Int = R.layout.controller_sharing_options
    private val excludedAccounts by lazy { args.getStringArrayList(EXTRA_EXCLUDED_ACCOUNTS).toHashSet() }
    private val restrictShareReduction by lazy { args.getBoolean(EXTRA_RESTRICT_SHARE_REDUCTION) }
    private val notUnselectableAccounts by lazy { args.getStringArrayList(EXTRA_NOT_UNSELECTEBLE_ACCOUNTS).toSortedSet() }
    override val viewModel: SharingOptionsViewModel by instance(arg = SharingOptionsViewModel.SharingOptionsParams(excludedAccounts))
    private val resultListener by lazy { targetController as OnSharingOptionsResult }
    private val adapter = BuildNetworkAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        require(targetController is OnSharingOptionsResult) {
            "$targetController must implement ${OnSharingOptionsResult::class.java.name}"
        }

        with(view) {
            toolbar.withActionButton(fromDictionary(R.string.sharing_options_button)) {
                resultListener.sharingOptions = getSelection()
                close()
            }
            tvPromotePostTitle.text = fromDictionary(R.string.sharing_options_promote_title)
            tvPromotePostDescription.text = fromDictionary(R.string.sharing_options_promote_description)
            tvMyNewsFeed.text = fromDictionary(R.string.sharing_options_newsfeed_title)
            tvConnections.text = fromDictionary(R.string.sharing_options_connections_title)

            var ignoreCheckedListener = false
            adapter.onSelectedAccountsChangedListener = {
                ignoreCheckedListener = true
                rbPromotePost.isChecked = false
                rbMyNewsFeed.isChecked = it.isEmpty()
                ignoreCheckedListener = false
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

        if (args.containsKey(EXTRA_PREDEFINED_OPTIONS)) {
            setSelection(args.getSerializable(EXTRA_PREDEFINED_OPTIONS) as ShareToOptions)
            args.remove(EXTRA_PREDEFINED_OPTIONS)
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

    private fun getSelection(): ShareToOptions {
        with(requireNotNull(view)) {
            return ShareToOptions(
                    privacyType = when {
                        rbPromotePost.isChecked -> PostPrivacyType.WORLD
                        rbMyNewsFeed.isChecked -> PostPrivacyType.PUBLIC
                        else -> PostPrivacyType.PRIVATE
                    },
                    selectedConnections = notUnselectableAccounts + adapter.selectedAccounts
            )
        }
    }

    private fun setSelection(options: ShareToOptions) {
        with(view ?: return) {
            rbPromotePost.isChecked = options.privacyType == PostPrivacyType.WORLD
            rbMyNewsFeed.isChecked = options.privacyType == PostPrivacyType.PUBLIC
            adapter.selectedAccounts = options.selectedConnections.toSet()

            if (restrictShareReduction) {
                if (options.privacyType == PostPrivacyType.PUBLIC) {
                    rvAllConnections.isGone = true
                }
                if (options.privacyType == PostPrivacyType.WORLD) {
                    rvAllConnections.isGone = true
                    rbMyNewsFeed.isGone = true
                }
            }
        }
    }

    companion object {
        private const val EXTRA_PREDEFINED_OPTIONS = "EXTRA_PREDEFINED_OPTIONS"
        private const val EXTRA_EXCLUDED_ACCOUNTS = "EXTRA_EXCLUDED_ACCOUNTS"
        private const val EXTRA_RESTRICT_SHARE_REDUCTION = "EXTRA_RESTRICT_SHARE_REDUCTION"
        private const val EXTRA_NOT_UNSELECTEBLE_ACCOUNTS = "EXTRA_NOT_UNSELECTEBLE_ACCOUNTS"

        fun <T> newInstance(
                accountsToExclude: List<String>,
                options: ShareToOptions = ShareToOptions.DEFAULT,
                listener: T,
                restrictShareReduction: Boolean = false): SharingOptionsController where T : OnSharingOptionsResult, T : Controller {
            val args = Bundle()
            args.putSerializable(EXTRA_PREDEFINED_OPTIONS, options)
            args.putStringArrayList(EXTRA_EXCLUDED_ACCOUNTS, accountsToExclude.toCollection(ArrayList()))
            args.putBoolean(EXTRA_RESTRICT_SHARE_REDUCTION, restrictShareReduction)
            if (restrictShareReduction) {
                args.putStringArrayList(EXTRA_NOT_UNSELECTEBLE_ACCOUNTS, options.selectedConnections.toCollection(ArrayList()))
            } else {
                args.putStringArrayList(EXTRA_NOT_UNSELECTEBLE_ACCOUNTS, ArrayList())
            }

            val result = SharingOptionsController(args)
            result.targetController = listener
            return result
        }
    }

    interface OnSharingOptionsResult {
        var sharingOptions: ShareToOptions
    }

    class ShareToOptions(
            var privacyType: PostPrivacyType,
            var selectedConnections: Set<String>
    ) : Serializable {


        val asPostPrivacy: PostPrivacyOptions
            get() {
                return PostPrivacyOptions(
                        privacyType = privacyType,
                        privacyConnections = selectedConnections
                )
            }

        suspend fun format(): CharSequence {
            return fromDictionary(R.string.need_create_share_to_prefix).format(
                    when {
                        privacyType == PostPrivacyType.WORLD -> fromDictionary(R.string.need_create_to_all_mnassa)
                        privacyType == PostPrivacyType.PUBLIC -> fromDictionary(R.string.need_create_to_newsfeed)
                        selectedConnections.isNotEmpty() -> {
                            val userInteractor: UserProfileInteractor = App.context.getInstance()
                            val usernames = selectedConnections.take(MAX_SHARE_TO_USERNAMES).mapNotNull { userInteractor.getProfileById(it) }.joinToString { it.userName }
                            if (selectedConnections.size <= 2) {
                                usernames
                            } else {
                                val tail = fromDictionary(R.string.need_create_to_connections_other).format(selectedConnections.size - 2)
                                "$usernames $tail"
                            }
                        }
                        else -> throw IllegalStateException()
                    }
            )
        }

        companion object {
            private const val MAX_SHARE_TO_USERNAMES = 2

            val DEFAULT = ShareToOptions(PostPrivacyType.PUBLIC, emptySet())
        }
    }
}