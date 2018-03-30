package com.mnassa.screen.profile.edit

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.EditText
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.dialog.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.widget.MnassaToolbar
import kotlinx.android.synthetic.main.header_main.view.*
import timber.log.Timber
import java.text.DateFormatSymbols
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/29/2018
 */
abstract class BaseEditableProfileController<VM : MnassaViewModel>(data: Bundle) : MnassaControllerImpl<VM>(data) {

    protected val dialog: DialogHelper by instance()
    protected var timeMillis: Long? = null

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        onActivityResult.subscribe {
            when (it.requestCode) {
                REQUEST_CODE_CROP -> {
                    when (it.resultCode) {
                        Activity.RESULT_OK -> {
                            val uri: Uri? = it.data?.getParcelableExtra(CropActivity.URI_PHOTO_RESULT)
                            uri?.let {
                                photoResult(it)
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

    protected fun addPhoto(fab: FloatingActionButton){
        fab.setOnClickListener {
            dialog.showSelectImageSourceDialog(it.context) { imageSource ->
                launchCoroutineUI {
                    activity?.let {
                        if (CropActivity.ImageSource.CAMERA == imageSource) {
                            val permissionsResult = permissions.requestPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            if (!permissionsResult.isAllGranted) {
                                return@launchCoroutineUI
                            }
                        }
                        val intent = CropActivity.start(imageSource, it)
                        startActivityForResult(intent, REQUEST_CODE_CROP)
                    }
                }
            }
        }
    }

    protected fun setCalendarEditText(editText: EditText){
        editText.isLongClickable = false
        editText.isFocusableInTouchMode = false
        editText.setOnClickListener {
            dialog.calendarDialog(editText.context, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                editText.setText("${DateFormatSymbols().months[month]} $dayOfMonth, $year")
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.YEAR, year)
                timeMillis = cal.timeInMillis
            })
        }
    }

    protected fun getDateByTimeMillis(createdAt: Long?): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = createdAt ?: return ""
        return "${DateFormatSymbols().months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
    }

    protected fun setToolbar(toolbar: MnassaToolbar, view: View){
        toolbar.onMoreClickListener = { proccesProfile(view) }
        toolbar.backButtonEnabled = true
        toolbar.ivToolbarMore.setImageResource(R.drawable.ic_check)
        toolbar.ivToolbarMore.setColorFilter(ContextCompat.getColor(view.context, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
        toolbar.ivToolbarMore.visibility = View.VISIBLE
    }

    abstract fun proccesProfile(view: View)
    abstract fun photoResult(uri: Uri)
    companion object {
        private const val REQUEST_CODE_CROP = 101
    }
}