package com.mnassa.screen.posts.need.create

import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.core.events.awaitFirst
import com.mnassa.domain.model.Post
import com.mnassa.extensions.formatAsMoney
import com.mnassa.google.PlayServiceHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_need_create.view.*
import kotlinx.coroutines.experimental.Job

/**
 * Created by Peter on 3/19/2018.
 */
class CreateNeedController(args: Bundle) : MnassaControllerImpl<CreateNeedViewModel>(args),
        SharingOptionsController.OnSharingOptionsResult {
    override val layoutId: Int = R.layout.controller_need_create
    override val viewModel: CreateNeedViewModel by instance()
    private var waitForResumeJob: Job? = null
    override var sharingOptions = SharingOptionsController.ShareToOptions.EMPTY
        set(value) {
            field = value

            waitForResumeJob?.cancel()
            waitForResumeJob = launchCoroutineUI {
                lifecycle.awaitFirst { it == Lifecycle.Event.ON_RESUME }
                view?.tvShareOptions?.text = formatShareToOptions(value)
            }
        }
    private val playServiceHelper: PlayServiceHelper by instance()
    private val attachedImagesAdapter = AttachedImagesRVAdapter()
    private var placeId: String? = null

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        playServiceHelper.googleApiClient.connect()

        with(view) {
            toolbar.withActionButton(fromDictionary(R.string.need_create_action_button)) {

            }
            tvShareOptions.setOnClickListener {
                val screen = SharingOptionsController.newInstance(sharingOptions)
                screen.targetController = this@CreateNeedController
                open(screen)
            }

            tvShareOptions.text = formatShareToOptions(sharingOptions)
            tilNeed.hint = fromDictionary(R.string.need_create_need_placeholder)

            chipTags.tvChipHeader.text = fromDictionary(R.string.need_create_tags_hint)
            chipTags.chipSearch = viewModel

            val placeAutocompleteAdapter = PlaceAutocompleteAdapter(context, viewModel)
            actvPlace.setAdapter(placeAutocompleteAdapter)
            actvPlace.setOnItemClickListener { _, _, i, _ ->
                val item = placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
                placeId = item.placeId
                val placeName = "${item.primaryText} ${placeAutocompleteAdapter.getItem(i)?.secondaryText}"
                actvPlace.setText(placeName)
            }
            tilPlace.hint = fromDictionary(R.string.need_create_city_hint)

            tvExtraDetails.text = fromDictionary(R.string.need_create_extra)
            tilPrice.hint = fromDictionary(R.string.need_create_price_hint)

            rvImages.layoutManager = object : LinearLayoutManager(context) {
                override fun canScrollHorizontally(): Boolean = false
                override fun canScrollVertically(): Boolean = false
            }
            rvImages.adapter = attachedImagesAdapter
        }

        if (args.containsKey(EXTRA_POST_TO_EDIT)) {
            setData(args.getSerializable(EXTRA_POST_TO_EDIT) as Post)
            args.remove(EXTRA_POST_TO_EDIT)
        }
    }

    private fun setData(post: Post) {
        with(view ?: return) {
            etNeed.setText(post.text)
            launchCoroutineUI {
                chipTags.setTags(post.tags.mapNotNull { viewModel.getTag(it) })
            }
            attachedImagesAdapter.set(post.images.map { AttachedImage.UploadedImage(it) })
            checkAbilityToAddNewImages()
            placeId = post.locationPlace?.placeId
            actvPlace.setText(post.locationPlace?.placeName?.toString())
            etPrice.setText(if (post.price > 0.0) post.price.formatAsMoney() else null )
        }
    }

    private fun checkAbilityToAddNewImages() {
        //TODO: control "add img" holder

    }

    private fun formatShareToOptions(options: SharingOptionsController.ShareToOptions): String {
        with(options) {
            return fromDictionary(R.string.need_create_share_to_prefix).format(
                    when {
                        isPromoted -> fromDictionary(R.string.need_create_to_all_mnassa)
                        isMyNewsFeedSelected -> fromDictionary(R.string.need_create_to_newsfeed)
                        selectedConnections.isNotEmpty() -> when {
                            options.selectedConnectionAccounts == null -> options.selectedConnections.size.toString() + " connections"
                            selectedConnections.size <= 2 -> options.selectedConnectionAccounts.take(2).joinToString { it.userName }
                            else -> {
                                val head = options.selectedConnectionAccounts.take(2).joinToString(",") { it.userName }
                                val tail = fromDictionary(R.string.need_create_to_connections_other).format(options.selectedConnections.size - 2)
                                "$head $tail"
                            }
                        }

                        else -> throw IllegalStateException()
                    }
            )
        }
    }

    override fun onViewDestroyed(view: View) {
        if (playServiceHelper.googleApiClient.isConnected) {
            playServiceHelper.googleApiClient.disconnect()
        }
        super.onViewDestroyed(view)
    }

    companion object {
        private const val EXTRA_POST_TO_EDIT = "EXTRA_POST_TO_EDIT"

        fun newInstance() = CreateNeedController(Bundle())
        fun newInstanceEditMode(post: Post): CreateNeedController {
            val args = Bundle()
            args.putSerializable(EXTRA_POST_TO_EDIT, post)
            return CreateNeedController(args)
        }
    }
}