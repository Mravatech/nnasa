package com.mnassa.screen.events.create

import android.Manifest
import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.StateExecutor
import com.mnassa.core.addons.WeakStateExecutor
import com.mnassa.core.addons.await
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.core.events.awaitFirst
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventType
import com.mnassa.extensions.centerOn
import com.mnassa.extensions.formatted
import com.mnassa.extensions.getLatLng
import com.mnassa.extensions.isGone
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.PlayServiceHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.events.create.date.DateTimePickerController
import com.mnassa.screen.posts.need.create.AttachedImage
import com.mnassa.screen.posts.need.create.AttachedImagesRVAdapter
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_event_create.view.*
import kotlinx.coroutines.experimental.Job
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/23/2018.
 */
class CreateEventController(args: Bundle) : MnassaControllerImpl<CreateEventViewModel>(args),
        SharingOptionsController.OnSharingOptionsResult,
        DateTimePickerController.OnDatePickerResultListener {
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
    private var dateTime: DateTimePickerController.DatePickerResult? = null
    private var placeId: String? = null
    set(value) {
        field = value
        value?.apply { centerMapOn(this) }
    }
    private val playServiceHelper: PlayServiceHelper by instance()
    private val googleMap = StateExecutor<GoogleMap?, GoogleMap>(initState = null, executionPredicate = { it != null})


    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view)
        playServiceHelper.googleApiClient.connect()

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
            toolbar.withActionButton(fromDictionary(R.string.tab_home_button_create_event)) {
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
            //
            tilEventTitle.hint = fromDictionary(R.string.event_create_title_placeholder)
            tilEventDateTime.hint = fromDictionary(R.string.event_create_date_placeholder)
            etEventDateTime.setOnClickListener {
                open(DateTimePickerController.newInstance(this@CreateEventController, dateTime))
            }
            //
            sLocation.adapter = ArrayAdapter(
                    context,
                    R.layout.support_simple_spinner_dropdown_item,
                    android.R.id.text1,
                    mutableListOf(
                            fromDictionary(R.string.event_location_specify),
                            fromDictionary(R.string.event_location_not_defined),
                            fromDictionary(R.string.event_location_later)))

            sLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    tilCity.isGone = position != EVENT_LOCATION_SPECIFY
                    tilAddress.isGone = position != EVENT_LOCATION_SPECIFY
                    mapView.isGone = position != EVENT_LOCATION_SPECIFY
                    if (position != EVENT_LOCATION_SPECIFY) {
                        placeId = null
                    }
                }
            }
            //
            val placeAutocompleteAdapter = PlaceAutocompleteAdapter(context, viewModel)
            actvCity.setAdapter(placeAutocompleteAdapter)
            actvCity.setOnItemClickListener { _, _, i, _ ->
                val item = placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
                placeId = item.placeId
                val placeName = "${item.primaryText} ${item.secondaryText}"
                actvCity.setText(placeName)
            }
            tilCity.hint = fromDictionary(R.string.need_create_city_hint)
            tilAddress.hint = fromDictionary(R.string.event_create_address)
            //
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync {
                with(it) {
                    uiSettings.isMapToolbarEnabled = false
                    uiSettings.isRotateGesturesEnabled = false
                    uiSettings.isScrollGesturesEnabled = false
                    uiSettings.isTiltGesturesEnabled = false
                    uiSettings.isZoomControlsEnabled = false
                    uiSettings.isZoomGesturesEnabled = false
                    setOnMapLoadedCallback {
                        googleMap.value = it
                    }
                }
            }
            placeId?.apply { centerMapOn(this) }
            //
            class FormattedEventType(val eventType: EventType) {
                override fun toString(): String = eventType.formatted.toString()
            }
            sEventType.adapter = ArrayAdapter(
                    context,
                    R.layout.support_simple_spinner_dropdown_item,
                    android.R.id.text1,
                    EventType.ALL.map { FormattedEventType(it) })
        }
    }

    private var marker: Marker? = null
    private fun centerMapOn(placeId: String) {
        launchCoroutineUI {
            playServiceHelper.googleApiClient.getLatLng(placeId)?.let { latLng ->
                val map = googleMap.await()
                map.centerOn(latLng)
                if (marker == null) {
                    marker = map.addMarker(MarkerOptions().position(latLng))
                } else {
                    requireNotNull(marker).position = latLng
                }

            }
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        view.mapView.onResume()
    }

    override fun onDetach(view: View) {
        view.mapView.onPause()
        super.onDetach(view)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        requireNotNull(view).mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView(view: View) {
        googleMap.value = null
        marker = null
        view.mapView.onDestroy()
        if (playServiceHelper.googleApiClient.isConnected) {
            playServiceHelper.googleApiClient.disconnect()
        }
        attachedImagesAdapter.destroyCallbacks()
        view.rvImages.adapter = null
        super.onDestroyView(view)
    }

    override fun onDateTimeResult(result: DateTimePickerController.DatePickerResult) {
        this.dateTime = result
        launchCoroutineUI {
            getViewSuspend().etEventDateTime.setText(result.format())
        }
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

        private const val EVENT_LOCATION_SPECIFY = 0
        private const val EVENT_LOCATION_NOT_DEFINED = 1
        private const val EVENT_LOCATION_LATER = 2


        fun newInstance(): CreateEventController = CreateEventController(Bundle())

        fun newInstance(event: EventModel): CreateEventController {
            val args = Bundle()
            args.putString(EXTRA_EVENT_ID, event.id)
            args.putSerializable(EXTRA_EVENT, event)
            return CreateEventController(args)
        }
    }
}