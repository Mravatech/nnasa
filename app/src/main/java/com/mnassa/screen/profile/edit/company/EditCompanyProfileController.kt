package com.mnassa.screen.profile.edit.company

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Patterns
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.dialog.DialogHelper
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.extensions.avatarSquare
import com.mnassa.google.PlayServiceHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.profile.model.ProfileModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_edit_company_profile.view.*
import kotlinx.android.synthetic.main.controller_organization_info.view.*
import kotlinx.android.synthetic.main.header_main.view.*
import kotlinx.android.synthetic.main.sub_company_info.view.*
import kotlinx.android.synthetic.main.sub_profile_avatar.view.*
import kotlinx.android.synthetic.main.sub_reg_company.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber
import java.text.DateFormatSymbols
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */

class EditCompanyProfileController(data: Bundle) : MnassaControllerImpl<EditCompanyProfileViewModel>(data) {
    override val layoutId = R.layout.controller_edit_company_profile
    override val viewModel: EditCompanyProfileViewModel by instance()
    private val accountModel: ProfileAccountModel by lazy { args.getParcelable(EXTRA_PROFILE) as ProfileAccountModel }
    private val playServiceHelper: PlayServiceHelper by instance()
    private val dialog: DialogHelper by instance()
    private var timeMillis: Long? = null

    private var companySelectedPlaceName: String? = null
    private var companySelectedPlaceId: String? = null

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        playServiceHelper.googleApiClient.connect()
        companySelectedPlaceId = accountModel.location?.placeId
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
        setupView(view)
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
        launchCoroutineUI {
            viewModel.imageUploadedChannel.consumeEach {
                view.ivUserAvatar.avatarSquare(it)
            }
        }
    }

    override fun onViewDestroyed(view: View) {
        super.onViewDestroyed(view)
        if (playServiceHelper.googleApiClient.isConnected) {
            playServiceHelper.googleApiClient.disconnect()
        }
        super.onDestroy()
    }

    private fun updateProfile(view: View) {
        val email = view.etCompanyEmail.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotBlank()) {
            view.etCompanyEmail.error = fromDictionary(R.string.email_is_not_valid)
            return
        }
        if (view.etCompanyUserName.text.isBlank()) {
            view.etCompanyUserName.error = fromDictionary(R.string.company_name_is_not_valid)
            return
        }
        viewModel.updateCompanyAccount(
                profileAccountModel = accountModel,
                userName = view.etCompanyUserName.text.toString(),
                showContactEmail = view.etCompanyEmail.isChosen,
                contactEmail = view.etCompanyEmail.text.toString(),
                founded = timeMillis,
                organizationType = view.vCompanyStatus.getOrganizationType(),
                website = view.etWebSite.text.toString(),
                foundedDate = view.etFoundation.text.toString(),
                locationId = companySelectedPlaceId,
                interests = view.chipCompanyInterests.getTags(),
                offers = view.chipCompanyOffers.getTags()
        )
    }

    private fun setupView(view: View) {
        view.toolbarEditProfile.title = fromDictionary(R.string.edit_profile_title)
        view.toolbarEditProfile.onMoreClickListener = { updateProfile(view) }
        view.toolbarEditProfile.backButtonEnabled = true
        view.toolbarEditProfile.ivToolbarMore.setImageResource(R.drawable.ic_check)
        view.toolbarEditProfile.ivToolbarMore.setColorFilter(ContextCompat.getColor(view.context, R.color.turquoiseBlue), android.graphics.PorterDuff.Mode.SRC_IN)
        view.toolbarEditProfile.ivToolbarMore.visibility = View.VISIBLE
        view.ivUserAvatar.avatarSquare(accountModel.avatar)
        view.tilCompanyName.hint = fromDictionary(R.string.reg_account_company_name)
        view.tilCompanyUserName.hint = fromDictionary(R.string.reg_personal_user_name)
        view.tilCompanyCity.hint = fromDictionary(R.string.reg_personal_city)
        view.chipCompanyOffers.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
        view.chipCompanyOffers.tvChipHeader.text = fromDictionary(R.string.reg_account_can_help_with)
        view.chipCompanyInterests.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
        view.chipCompanyInterests.tvChipHeader.text = fromDictionary(R.string.reg_account_interested_in)
        view.chipCompanyOffers.chipSearch = viewModel
        view.chipCompanyInterests.chipSearch = viewModel
        view.tilCompanyCity.hint = fromDictionary(R.string.reg_personal_city)
        view.tvHeader.text = fromDictionary(R.string.reg_company_title)
        view.btnHeaderNext.text = fromDictionary(R.string.reg_info_next)
        view.tilWebSite.hint = fromDictionary(R.string.reg_company_website)
        view.tilCompanyEmail.hint = fromDictionary(R.string.reg_info_email)
        view.tilFoundation.hint = fromDictionary(R.string.reg_company_founded)
        view.tvSkipThisStep.text = fromDictionary(R.string.reg_info_skip)
        val placeAutocompleteAdapter = PlaceAutocompleteAdapter(view.context, viewModel)
        view.actvCompanyCity.setText(accountModel.location?.en?.placeName)
        view.actvCompanyCity.setAdapter(placeAutocompleteAdapter)
        view.actvCompanyCity.setOnItemClickListener({ _, _, i, _ ->
            placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
            companySelectedPlaceId = placeAutocompleteAdapter.getItem(i)?.placeId
            companySelectedPlaceName = "${placeAutocompleteAdapter.getItem(i)?.primaryText} ${placeAutocompleteAdapter.getItem(i)?.secondaryText}"
            view.actvCompanyCity.setText(companySelectedPlaceName ?: "")
        })
    }

    companion object {
        private const val REQUEST_CODE_CROP = 101
        private const val EXTRA_PROFILE = "EXTRA_PROFILE"

        fun newInstance(profileModel: ProfileModel): EditCompanyProfileController {
            val params = Bundle()
            params.putParcelable(EXTRA_PROFILE, profileModel.profile)
            return EditCompanyProfileController(params)
        }
    }

}