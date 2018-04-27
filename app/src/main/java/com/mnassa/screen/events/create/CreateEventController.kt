package com.mnassa.screen.events.create

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.StateExecutor
import com.mnassa.core.addons.await
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.core.events.awaitFirst
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.LocationPlaceModelImpl
import com.mnassa.extensions.*
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.PlayServiceHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.events.create.date.DateTimePickerController
import com.mnassa.screen.posts.need.create.AttachedImage
import com.mnassa.screen.posts.need.create.AttachedImagesRVAdapter
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_event_create.view.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/23/2018.
 */
class CreateEventController(args: Bundle) : MnassaControllerImpl<CreateEventViewModel>(args),
        SharingOptionsController.OnSharingOptionsResult,
        DateTimePickerController.OnDatePickerResultListener {
    override val layoutId: Int = R.layout.controller_event_create
    private val eventId: String? by lazy { args.getString(EXTRA_EVENT_ID, null) }
    private var event: EventModel? = null
    override val viewModel: CreateEventViewModel by instance(arg = eventId)
    private var waitForResumeJob: Job? = null
    override var sharingOptions = SharingOptionsController.ShareToOptions.DEFAULT
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
    private var dateTime: DateTimePickerController.DatePickerResult? = null
    private var eventStatus: EventStatus = EventStatus.OPENED
    private var placeId: String? = null
        set(value) {
            field = value
            value?.apply { centerMapOn(this) }
            onEventChanged()
        }
    private var placeLatLng: LatLng? = null
    private val playServiceHelper: PlayServiceHelper by instance()
    private val googleMap = StateExecutor<GoogleMap?, GoogleMap>(initState = null, executionPredicate = { it != null })


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
            toolbar.withActionButton(fromDictionary(R.string.event_post_button)) {
                val imagesToUpload = attachedImagesAdapter.dataStorage.filterIsInstance<AttachedImage.LocalImage>().map { it.imageUri }
                val uploadedImages = attachedImagesAdapter.dataStorage.filterIsInstance<AttachedImage.UploadedImage>().map { it.imageUrl }

                val model = CreateOrEditEventModel(
                        id = eventId,
                        title = etEventTitle.text.toString(),
                        description = etEventDescription.text.toString(),
                        type = (sEventType.selectedItem as FormattedEventType).eventType,
                        startDateTime = requireNotNull(dateTime).startDateTime,
                        durationMillis = requireNotNull(dateTime).durationMillis,
                        imagesToUpload = imagesToUpload,
                        uploadedImages = uploadedImages.toMutableSet(),
                        privacy = sharingOptions.asPostPrivacy,
                        ticketsTotal = etTicketsQuantity.text.toString().toInt(),
                        ticketsPerAccount = etTicketsPerAccountLimit.text.toString().toInt(),
                        price = etTicketPrice.text.toString().toLongOrNull()?.takeIf { switchPaidEvent.isChecked },
                        locationType = getLocationType(),
                        locationDescription = getLocationDescription(),
                        tagModels = chipTags.getTags(),
                        status = eventStatus
                )
                viewModel.publish(model)
            }
            tvShareOptions.setOnClickListener {
                val event = event
                open(SharingOptionsController.newInstance(
                        options = sharingOptions,
                        listener = this@CreateEventController,
                        accountsToExclude = if (event != null) listOf(event.author.id) else emptyList(),
                        restrictShareReduction = eventId != null))
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
                    listOf(
                            fromDictionary(R.string.event_location_specify),
                            fromDictionary(R.string.event_location_not_defined),
                            fromDictionary(R.string.event_location_later)))

            sLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = onEventChanged()

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    tilCity.isGone = position != EVENT_LOCATION_SPECIFY
                    tilAddress.isGone = position != EVENT_LOCATION_SPECIFY
                    mapView.isGone = position != EVENT_LOCATION_SPECIFY
                    if (position != EVENT_LOCATION_SPECIFY) {
                        placeId = null
                    }
                    onEventChanged()
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
            val eventTypes = listOf(EventType.LECTURE, EventType.DISCUSSION, EventType.WORKSHOP, EventType.EXERCISE, EventType.ACTIVITY)
            sEventType.adapter = ArrayAdapter(
                    context,
                    R.layout.support_simple_spinner_dropdown_item,
                    android.R.id.text1,
                    eventTypes.map { FormattedEventType(it) })
            sEventType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = onEventChanged()

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    onEventChanged()
                }
            }
            //
            chipTags.tvChipHeader.text = fromDictionary(R.string.need_create_tags_hint)
            chipTags.chipSearch = viewModel
            //
            rvImages.adapter = attachedImagesAdapter
            //
            switchPaidEvent.setOnCheckedChangeListener { buttonView, isChecked ->
                flTicketPrice.isGone = !isChecked
                onEventChanged()
            }
            //
            tilEventDescription.hint = fromDictionary(R.string.event_description_placeholder)
            tilTicketPrice.hint = fromDictionary(R.string.event_ticket_price_placeholder)
            tilTicketsQuantity.hint = fromDictionary(R.string.event_tickets_quantity_placeholder)
            tilTicketsPerAccountLimit.hint = fromDictionary(R.string.event_limit_tickets_per_person_placeholder)
            //
            etEventTitle.addTextChangedListener(SimpleTextWatcher { onEventChanged() })
            etAddress.addTextChangedListener(SimpleTextWatcher { onEventChanged() })
            etEventDescription.addTextChangedListener(SimpleTextWatcher { onEventChanged() })
            etTicketPrice.addTextChangedListener(SimpleTextWatcher { onEventChanged() })
            etTicketsQuantity.addTextChangedListener(SimpleTextWatcher { onEventChanged() })
            etTicketsPerAccountLimit.addTextChangedListener(SimpleTextWatcher { onEventChanged() })
            //
            onEventChanged()
        }

        launchCoroutineUI { viewModel.closeScreenChannel.consumeEach { close() } }

        if (args.containsKey(EXTRA_EVENT)) {
            setData(args[EXTRA_EVENT] as EventModel)
            args.remove(EXTRA_EVENT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE_CROP) return
        when (resultCode) {
            Activity.RESULT_OK -> {
                val uri: Uri? = data?.getParcelableExtra(CropActivity.URI_PHOTO_RESULT)
                uri?.let {
                    val imageToReplaceLocal = imageToReplace
                    val newImage = AttachedImage.LocalImage(uri)
                    if (imageToReplaceLocal != null) {
                        attachedImagesAdapter.replace(imageToReplaceLocal, newImage)
                    } else {
                        attachedImagesAdapter.dataStorage.add(newImage)
                    }
                    imageToReplace = null
                }
                onEventChanged()
            }
            CropActivity.GET_PHOTO_ERROR -> {
                imageToReplace = null
            }
        }
    }

    private fun onEventChanged() {
        val view = view ?: return
        view.toolbar.actionButtonClickable = canCreateEvent()
    }

    private fun canCreateEvent(): Boolean {
        with(view ?: return false) {
            if (etEventTitle.text.isBlank()) return false
            val dateTime = dateTime ?: return false
            if (dateTime.durationMillis == 0L || dateTime.startDateTime.time < System.currentTimeMillis()) return false
            when (sLocation.selectedItemPosition) {
                EVENT_LOCATION_SPECIFY -> {
                    if (placeId == null) return false
                    if (etAddress.text.isBlank()) return false
                }
            }
            if (etEventDescription.text.isBlank()) return false
            if (attachedImagesAdapter.dataStorage.size == 0) return false
            if (switchPaidEvent.isChecked) {
                if (etTicketPrice.text.isBlank()) return false
            }
            if (etTicketsQuantity.text.isBlank()) return false
            if (etTicketsPerAccountLimit.text.isBlank()) return false

            val quantity = etTicketsQuantity.text.toString().toIntOrNull() ?: return false
            val ticketsLimit = etTicketsPerAccountLimit.text.toString().toIntOrNull()
                    ?: return false
            if (ticketsLimit > quantity) return false
        }

        return true
    }

    private fun getLocationType(): EventLocationType {
        val view = requireNotNull(view)
        return when (view.sLocation.selectedItemPosition) {
            EVENT_LOCATION_SPECIFY -> {
                val latLng = requireNotNull(placeLatLng)
                val placeId = requireNotNull(placeId)
                val place = LocationPlaceModelImpl(
                        null,
                        latLng.latitude,
                        latLng.longitude,
                        placeId,
                        null
                )
                EventLocationType.Specified(place, placeId)
            }
            EVENT_LOCATION_NOT_DEFINED -> EventLocationType.NotDefined
            EVENT_LOCATION_LATER -> EventLocationType.Later
            else -> throw IllegalStateException("Invalid location type: ${view.sLocation.selectedItemPosition}")
        }
    }

    private fun getLocationDescription(): String? {
        val view = requireNotNull(view)
        return view.etAddress.text.toString().takeIf { view.sLocation.selectedItemPosition == EVENT_LOCATION_SPECIFY }
    }

    private fun setData(event: EventModel) {
        this.event = event
        this.eventStatus = event.status
        with(view ?: return) {
            toolbar.title = fromDictionary(R.string.event_edit_title)

            dateTime = DateTimePickerController.DatePickerResult(event.startAt, event.duration?.toMillis()
                    ?: 0L)
            etEventDateTime.setText(requireNotNull(dateTime).format())
            val locationType = event.locationType
            when (locationType) {
                is EventLocationType.Specified -> {
                    sLocation.setSelection(EVENT_LOCATION_SPECIFY)
                    placeId = locationType.location.placeId
                    val lat = locationType.location.lat
                    val lng = locationType.location.lng
                    if (lat != null && lng != null) {
                        placeLatLng = LatLng(lat, lng)
                    }
                    etAddress.setText(locationType.location.city?.toString())
                    actvCity.setText(locationType.location.placeName?.toString())
                }
                is EventLocationType.NotDefined -> {
                    sLocation.setSelection(EVENT_LOCATION_NOT_DEFINED)
                }
                is EventLocationType.Later -> {
                    sLocation.setSelection(EVENT_LOCATION_LATER)
                }
            }

            sharingOptions.selectedConnections = event.privacyConnections
            launchCoroutineUI {
                tvShareOptions.text = sharingOptions.format()
            }
            attachedImagesAdapter.set(event.pictures.map { AttachedImage.UploadedImage(it) })
            etTicketPrice.setText(event.price.toString())
            switchPaidEvent.isChecked = event.price > 0
            launchCoroutineUI {
                chipTags.setTags(event.tags.mapNotNull { viewModel.getTag(it) })
            }
            etEventTitle.setText(event.title)
            etEventDescription.setText(event.text)
            etTicketsPerAccountLimit.setText(event.ticketsPerAccount.toString())
            etTicketsQuantity.setText(event.ticketsTotal.toString())
            sEventType.setSelection(event.type.position)
            onEventChanged()
        }
    }

    private var marker: Marker? = null
    private fun centerMapOn(placeId: String) {
        launchCoroutineUI {
            playServiceHelper.googleApiClient.getLatLng(placeId)?.let { latLng ->
                placeLatLng = latLng
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
        onEventChanged()
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

    class FormattedEventType(val eventType: EventType) {
        override fun toString(): String = eventType.formatted.toString()
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