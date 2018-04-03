package com.mnassa.screen.accountinfo.personal

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.request.RequestOptions
import com.github.salomonbrys.kodein.instance
import com.google.firebase.storage.StorageReference
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.dialog.DialogHelper
import com.mnassa.domain.model.AccountType
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.activity.CropActivity
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.GlideApp
import com.mnassa.module.GlideApp
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.buildnetwork.BuildNetworkController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_personal_info.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber
import java.text.DateFormatSymbols
import java.util.*


/**
 * Created by Peter on 2/27/2018.
 */

class PersonalInfoController(data: Bundle
) : MnassaControllerImpl<PersonalInfoViewModel>(data
) {

    override val layoutId: Int = R.layout.controller_personal_info
    override val viewModel: PersonalInfoViewModel by instance()

    private val accountModel: ShortAccountModel by lazy { args.getSerializable(EXTRA_ACCOUNT) as ShortAccountModel }
    private val dialog: DialogHelper by instance()
    private var timeMillis: Long? = null

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.tilDateOfBirthday.hint = fromDictionary(R.string.reg_person_info_birthday)
        view.tilPhoneNumber.hint = fromDictionary(R.string.reg_person_info_phone)
        view.tvInfoGender.text = fromDictionary(R.string.reg_person_info_gender)
        view.rInfoBtnMale.text = fromDictionary(R.string.reg_person_info_male_gender)
        view.rInfoBtnFemale.text = fromDictionary(R.string.reg_person_info_female_gender)
        view.tilYourEmail.hint = fromDictionary(R.string.reg_person_info_email)
        view.etPhoneNumber.setText(accountModel.contactPhone)
        view.tvSkipThisStep.text = fromDictionary(R.string.reg_info_skip)
        view.etDateOfBirthday.isLongClickable = false
        view.etDateOfBirthday.isFocusableInTouchMode = false
        view.etDateOfBirthday.setOnClickListener {
            dialog.calendarDialog(view.context, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                view.etDateOfBirthday.setText("${DateFormatSymbols().months[month]} $dayOfMonth, $year")
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.YEAR, year)
                timeMillis = cal.timeInMillis
            })
        }
        view.fabInfoAddPhoto.setOnClickListener {
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
        view.tvHeader.text = fromDictionary(R.string.reg_personal_info_title)
        view.btnHeaderNext.text = fromDictionary(R.string.reg_info_next)
        view.btnHeaderNext.setOnClickListener {
            val email = view.etYourEmail.text.toString()
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotBlank()) {
                view.etYourEmail.error = fromDictionary(R.string.email_is_not_valid)
                return@setOnClickListener
            }
            viewModel.processAccount(accountModel,
                    view.etPhoneNumber.text.toString(),
                    view.containerSelectOccupation.getAllAbilities(),
                    view.etDateOfBirthday.text.toString(),
                    view.etYourEmail.isChosen,
                    timeMillis,
                    view.etPhoneNumber.isChosen,
                    view.etYourEmail.text.toString()
            )
        }
        onActivityResult.subscribe {
            when (it.requestCode) {
                REQUEST_CODE_CROP -> {
                    when (it.resultCode) {
                        Activity.RESULT_OK -> {
                            val uri: Uri? = it.data?.getParcelableExtra(CropActivity.URI_PHOTO_RESULT)
                            uri?.let {
                                viewModel.uploadPhotoToStorage(it)
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
        launchCoroutineUI {
            viewModel.imageUploadedChannel.consumeEach {
                setImage(view.ivUserAvatar, it)
            }
        }
        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                val controller = when (it) {
                    is PersonalInfoViewModel.OpenScreenCommand.InviteScreen -> {
                        BuildNetworkController.newInstance()
                    }
                }
                open(controller)
            }
        }
        val acc = AccountType.PERSONAL
        Timber.i(acc.name.toLowerCase())
    }

    private fun setImage(imageView: ImageView, result: StorageReference?) {
        val requestOptions = RequestOptions().placeholder(R.drawable.ic_empty_avatar_placeholder).error(R.drawable.ic_empty_avatar_placeholder)
        GlideApp.with(imageView)
                .load(result)
                .apply(requestOptions)
                .apply(RequestOptions.centerCropTransform())
                .into(imageView)
        //todo move to extension after merge
    }

    companion object {
        private const val REQUEST_CODE_CROP = 101
        private const val EXTRA_ACCOUNT = "EXTRA_ACCOUNT"

        const val OTHER = 4

        fun newInstance(ac: ShortAccountModel): PersonalInfoController {
            val params = Bundle()
            params.putSerializable(EXTRA_ACCOUNT, ac)
            return PersonalInfoController(params)
        }
    }
}