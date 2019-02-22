package com.mnassa.screen.profile.edit

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import com.mnassa.widget.MnassaToolbar
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.suspendCancellableCoroutine
import org.kodein.di.generic.instance
import timber.log.Timber
import java.text.DateFormatSymbols
import java.util.*
import kotlin.coroutines.resume

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/29/2018
 */
abstract class BaseEditableProfileController<VM : BaseEditableProfileViewModel>(data: Bundle) : MnassaControllerImpl<VM>(data) {

    protected val accountModel: ProfileAccountModel by lazy {
        args.getSerializable(EXTRA_PROFILE) as ProfileAccountModel
    }
    protected val interests: List<TagModel> by lazy { args.getParcelableArrayList<TagModel>(EXTRA_TAGS_INTERESTS) as java.util.ArrayList<TagModel> }
    protected val offers: List<TagModel> by lazy { args.getParcelableArrayList<TagModel>(EXTRA_TAGS_OFFERS) as java.util.ArrayList<TagModel> }

    protected val dialog: DialogHelper by instance()
    protected var birthday: Long? = null

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        onActivityResult.subscribe {
            when (it.requestCode) {
                REQUEST_CODE_CROP -> {
                    when (it.resultCode) {
                        Activity.RESULT_OK -> {
                            val uri: Uri? = it.data?.getParcelableExtra(CropActivity.URI_PHOTO_RESULT)
                            uri?.let {
                                photoResult(it, view)
                            } ?: run {
                                Timber.i("uri is null")
                            }
                        }
                        CropActivity.GET_PHOTO_ERROR -> {
                            Timber.i("CropActivity.GET_PHOTO_ERROR")
                        }
                    }
                }
            }
        }
    }

    protected fun addPhoto(fab: FloatingActionButton) {
        fab.setOnClickListener {
            dialog.showSelectImageSourceDialog(it.context) { imageSource ->
                launchCoroutineUI {
                    activity?.let {
                        when (imageSource) {
                            CropActivity.ImageSource.CAMERA -> {
                                val permissionsResult = permissions.requestPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                if (!permissionsResult.isAllGranted) {
                                    return@launchCoroutineUI
                                }
                            }
                            CropActivity.ImageSource.GALLERY -> {
                                val permissionsResult = permissions.requestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                                if (!permissionsResult.isAllGranted) {
                                    return@launchCoroutineUI
                                }
                            }
                        }
                        val intent = CropActivity.start(imageSource, it, cropSquare = true)
                        startActivityForResult(intent, REQUEST_CODE_CROP)
                    }
                }
            }
        }
    }

    protected fun setCalendarEditText(editText: EditText) {
        editText.isLongClickable = false
        editText.isFocusableInTouchMode = false
        editText.setOnClickListener {
            dialog.calendarDialogPast(editText.context, birthday?.let { Date(it) }, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                editText.setText("${DateFormatSymbols().months[month]} $dayOfMonth, $year")
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.YEAR, year)
                birthday = cal.timeInMillis
            })
        }
    }

    protected fun getDateByTimeMillis(createdAt: Long?): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = createdAt ?: return ""
        return "${DateFormatSymbols().months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
    }

    protected fun setToolbar(toolbar: MnassaToolbar, view: View) {
        toolbar.backButtonEnabled = true
        toolbar.withActionButton(fromDictionary(R.string.edit_save)) {
            launchCoroutineUI { preProcessProfile(view) }
        }
    }

    protected suspend fun formatTagLabel(prefix: String): CharSequence {
        val reward = viewModel.addTagRewardChannel.consume { receive() } ?: return prefix
        val result = SpannableString(fromDictionary(R.string.add_tags_reward_suffix).format(prefix, reward))
        result.setSpan(ForegroundColorSpan(ContextCompat.getColor(getViewSuspend().context, R.color.accent)), prefix.length, result.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return result
    }

    private suspend fun preProcessProfile(view: View) {
        val interestsDiff = maxOf(interests.size - getEnteredInterests().size, 0)
        val offersDiff = maxOf(offers.size - getEnteredOffers().size, 0)

        if (interestsDiff > 0 || offersDiff > 0) {
            val price = viewModel.calculateDeleteTagsPrice(interestsDiff + offersDiff)
            if (price != null) {
                val dialogResult = suspendCancellableCoroutine<Int> { continuation ->
                    dialog.showDeleteProfileTagsDialog(view.context, price, onConfirm = {
                        continuation.resume(OK_CLICK)
                    }, onCancel = { continuation.resume(CANCEL_CLICK) })
                }
                if (dialogResult == CANCEL_CLICK) return
            }
        }

        processProfile(view)
    }

    abstract suspend fun processProfile(view: View)
    abstract fun photoResult(uri: Uri, view: View)
    open suspend fun getEnteredInterests(): List<TagModel> = emptyList()
    open suspend fun getEnteredOffers(): List<TagModel> = emptyList()

    companion object {
        private const val REQUEST_CODE_CROP = 101
        private const val OK_CLICK = 1
        private const val CANCEL_CLICK = -1

        const val EXTRA_PROFILE = "EXTRA_PROFILE"
        const val EXTRA_TAGS_INTERESTS = "EXTRA_TAGS_INTERESTS"
        const val EXTRA_TAGS_OFFERS = "EXTRA_TAGS_OFFERS"
    }
}