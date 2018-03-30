package com.mnassa.screen.posts.need.sharing

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.PostPrivacyType
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.buildnetwork.BuildNetworkAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_sharing_options.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import java.io.Serializable

/**
 * Created by Peter on 3/21/2018.
 */
class SharingOptionsController(args: Bundle) : MnassaControllerImpl<SharingOptionsViewModel>(args) {
    override val layoutId: Int = R.layout.controller_sharing_options
    override val viewModel: SharingOptionsViewModel by instance()
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
            rvPromotePostRoot.setOnClickListener {
                if (!rbPromotePost.isChecked) {
                    rbPromotePost.isChecked = true
                }
            }
            rvMyNewsFeedRoot.setOnClickListener {
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
                adapter.set(it)
            }
        }
    }

    override fun onDestroyView(view: View) {
        adapter.destroyCallbacks()
        super.onDestroyView(view)
    }

    private fun getSelection(): ShareToOptions {
        with(requireNotNull(view)) {
            return ShareToOptions(
                    isPromoted = rbPromotePost.isChecked,
                    isMyNewsFeedSelected = rbMyNewsFeed.isChecked,
                    selectedConnections = adapter.selectedAccounts.toList()
            )
        }
    }

    private fun setSelection(options: ShareToOptions) {
        with(view ?: return) {
            rbPromotePost.isChecked = options.isPromoted
            rbMyNewsFeed.isChecked = options.isMyNewsFeedSelected
            adapter.selectedAccounts = options.selectedConnections.toSet()
        }
    }

    companion object {
        private const val EXTRA_PREDEFINED_OPTIONS = "EXTRA_PREDEFINED_OPTIONS"

        fun newInstance(options: ShareToOptions = ShareToOptions.EMPTY): SharingOptionsController {
            val args = Bundle()
            args.putSerializable(EXTRA_PREDEFINED_OPTIONS, options)
            return SharingOptionsController(args)
        }
    }

    interface OnSharingOptionsResult {
        var sharingOptions: ShareToOptions
    }

    class ShareToOptions(
            var isPromoted: Boolean,
            var isMyNewsFeedSelected: Boolean,
            var selectedConnections: List<String>
    ) : Serializable {

        val privacyType: PostPrivacyType get() {
            return when {
                isPromoted -> PostPrivacyType.WORLD
                isMyNewsFeedSelected -> PostPrivacyType.PUBLIC
                else -> PostPrivacyType.PRIVATE
            }
        }

        companion object {
            val EMPTY = ShareToOptions(false, true, emptyList())
        }
    }
}