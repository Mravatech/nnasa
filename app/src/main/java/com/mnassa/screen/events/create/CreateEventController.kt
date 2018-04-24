package com.mnassa.screen.events.create

import android.Manifest
import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.core.events.awaitFirst
import com.mnassa.domain.model.EventModel
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.create.AttachedImage
import com.mnassa.screen.posts.need.create.AttachedImagesRVAdapter
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_event_create.view.*
import kotlinx.coroutines.experimental.Job
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/23/2018.
 */
class CreateEventController(args: Bundle) : MnassaControllerImpl<CreateEventViewModel>(args),
        SharingOptionsController.OnSharingOptionsResult {
    override val layoutId: Int = R.layout.controller_event_create
    private val eventId by lazy { args.getString(EXTRA_EVENT_ID, null) }
    override val viewModel: CreateEventViewModel by instance(arg = eventId)
    private var waitForResumeJob: Job? = null
    override var sharingOptions = SharingOptionsController.ShareToOptions.EMPTY
        set(value) {
            field = value

            waitForResumeJob?.cancel()
            waitForResumeJob = launchCoroutineUI {
                lifecycle.awaitFirst { it == Lifecycle.Event.ON_RESUME }
                view?.tvShareOptions?.text = value.format()
            }
        }
    private val dialogHelper: DialogHelper by instance()
    private val attachedImagesAdapter = AttachedImagesRVAdapter()
    private var imageToReplace: AttachedImage? = null
    private var event: EventModel? = null


    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        attachedImagesAdapter.onAddImageClickListener = {
            dialogHelper.showSelectImageSourceDialog(view.context) { imageSource -> launchCoroutineUI { selectImage(imageSource) } }
        }
        attachedImagesAdapter.onRemoveImageClickListener = { _, item ->
            attachedImagesAdapter.dataStorage.remove(item)
        }
        attachedImagesAdapter.onReplaceImageClickListener = { _, item ->
            imageToReplace = item
            attachedImagesAdapter.onAddImageClickListener()
        }

        with(view) {
            toolbar.withActionButton(fromDictionary(R.string.need_create_action_button)) {
                //                viewModel.createPost(
//                        need = etNeed.text.toString(),
//                        tags = chipTags.getTags(),
//                        images = attachedImagesAdapter.dataStorage.toList(),
//                        placeId = placeId,
//                        price = etPrice.text.toString().toLongOrNull(),
//                        postPrivacyOptions = sharingOptions.asPostPrivacy
//                )
            }
            tvShareOptions.setOnClickListener {
                val event = event
                open(SharingOptionsController.newInstance(
                        options = sharingOptions,
                        listener = this@CreateEventController,
                        accountsToExclude = if (event != null) listOf(event.author.id) else emptyList()))
            }

            launchCoroutineUI {
                tvShareOptions.text = sharingOptions.format()
            }
        }
    }

    override fun onDestroyView(view: View) {
        attachedImagesAdapter.destroyCallbacks()
        view.rvImages.adapter = null
        super.onDestroyView(view)
    }

    private suspend fun selectImage(imageSource: CropActivity.ImageSource) {
        val permissionsList = when (imageSource) {
            CropActivity.ImageSource.GALLERY -> listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            CropActivity.ImageSource.CAMERA -> listOf(Manifest.permission.CAMERA)
        }
        val permissionResult = permissions.requestPermissions(permissionsList)
        if (permissionResult.isAllGranted) {
            activity?.let {
                val intent = CropActivity.start(imageSource, it)
                startActivityForResult(intent, REQUEST_CODE_CROP)
            }
        }
    }

    companion object {
        private const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"
        private const val EXTRA_EVENT = "EXTRA_EVENT"
        private const val REQUEST_CODE_CROP = 101

        fun newInstance(): CreateEventController = CreateEventController(Bundle())

        fun newInstance(event: EventModel): CreateEventController {
            val args = Bundle()
            args.putString(EXTRA_EVENT_ID, event.id)
            args.putSerializable(EXTRA_EVENT, event)
            return CreateEventController(args)
        }
    }
}