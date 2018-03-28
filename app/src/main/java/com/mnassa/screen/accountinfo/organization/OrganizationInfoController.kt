package com.mnassa.screen.accountinfo.organization

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.dialog.DialogHelper
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.avatarSquare
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.buildnetwork.BuildNetworkController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_organization_info.view.*
import kotlinx.android.synthetic.main.sub_company_info.view.*
import kotlinx.android.synthetic.main.sub_profile_avatar.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber
import java.text.DateFormatSymbols
import java.util.*

/**
 * Created by Peter on 2/28/2018.
 */
class OrganizationInfoController(data: Bundle
) : MnassaControllerImpl<OrganizationInfoViewModel>(
        data
) {
    override val layoutId: Int = R.layout.controller_organization_info
    override val viewModel: OrganizationInfoViewModel by instance()
    private val accountModel: ShortAccountModel by lazy { args.getSerializable(EXTRA_ACCOUNT) as ShortAccountModel }
    private val dialog: DialogHelper by instance()
    private var timeMillis: Long? = null

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        setupViews(view)
        view.etFoundation.isLongClickable = false
        view.etFoundation.isFocusableInTouchMode = false
        view.etFoundation.setOnClickListener {
            dialog.calendarDialog(view.context, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                view.etFoundation.setText("${DateFormatSymbols().months[month]} $dayOfMonth, $year")
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
//        view.etCompanyName.setText(accountModel.organizationInfo?.organizationName)
//        view.etPhoneNumber.setHideMode(false)
        view.etCompanyEmail.setHideMode(false)
        view.btnHeaderNext.setOnClickListener {
            val email = view.etCompanyEmail.text.toString()
//            if (view.etCompanyName.text.toString().isBlank()) {
//                view.etCompanyName.error = fromDictionary(R.string.company_name_is_not_valid)
//                return@setOnClickListener
//            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotBlank()) {
                view.etCompanyEmail.error = fromDictionary(R.string.email_is_not_valid)
                return@setOnClickListener
            }
            viewModel.processAccount(accountModel,
//                    view.etPhoneNumber.text.toString().takeIf { it.isNotBlank() },
                    view.vCompanyStatus.getOrganizationType(),
                    view.etFoundation.text.toString().takeIf { it.isNotBlank() },
                    view.etCompanyEmail.isChosen,
                    timeMillis,
//                    view.etPhoneNumber.isChosen,
                    view.etCompanyEmail.text.toString().takeIf { it.isNotBlank() },
//                    view.etCompanyName.text.toString(),
                    view.etWebSite.text.toString().takeIf { it.isNotBlank() }
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
                view.ivUserAvatar.avatarSquare(it)
            }
        }
        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                val controller = when (it) {
                    is OrganizationInfoViewModel.OpenScreenCommand.InviteScreen -> {
                        BuildNetworkController.newInstance()
                    }
                }
                open(controller)
            }
        }
    }

    private fun setupViews(view: View) {
        view.tvHeader.text = fromDictionary(R.string.reg_company_title)
        view.btnHeaderNext.text = fromDictionary(R.string.reg_info_next)
        view.tilWebSite.hint = fromDictionary(R.string.reg_company_website)
//        view.tilPhoneNumber.hint = fromDictionary(R.string.reg_info_phone_number)
        view.tilCompanyEmail.hint = fromDictionary(R.string.reg_info_email)
//        view.tilCompanyName.hint = fromDictionary(R.string.reg_company_name)
        view.tilFoundation.hint = fromDictionary(R.string.reg_company_founded)
        view.tvSkipThisStep.text = fromDictionary(R.string.reg_info_skip)
    }

    companion object {
        private const val REQUEST_CODE_CROP = 101
        private const val EXTRA_ACCOUNT = "EXTRA_ACCOUNT"
//        fun newInstance() = OrganizationInfoController()

        fun newInstance(ac: ShortAccountModel
        ): OrganizationInfoController {
            val params = Bundle()
            params.putSerializable(EXTRA_ACCOUNT, ac)
            return OrganizationInfoController(params
            )
        }
    }
}