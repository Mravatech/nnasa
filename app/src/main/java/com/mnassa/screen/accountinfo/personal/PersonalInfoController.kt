package com.mnassa.screen.accountinfo.personal

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.mnassa.App.Companion.context
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.extensions.PATTERN_PHONE
import com.mnassa.extensions.avatarSquare
import com.mnassa.screen.buildnetwork.BuildNetworkController
import com.mnassa.screen.profile.edit.BaseEditableProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_personal_info.view.*
import kotlinx.android.synthetic.main.sub_personal_info.view.*
import kotlinx.android.synthetic.main.sub_profile_avatar.*
import kotlinx.android.synthetic.main.sub_profile_avatar.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance
import org.kodein.di.generic.kcontext

class PersonalInfoController(data: Bundle) : BaseEditableProfileController<PersonalInfoViewModel>(data) {



    override val layoutId: Int = R.layout.controller_personal_info
    override val viewModel: PersonalInfoViewModel by instance()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)


        setupViews(view)
        with(view) {

            etPhoneNumber.setText(accountModel.contactPhone)
            etPhoneNumber.setHideMode(false)
            etYourEmail.setHideMode(false)
            tvSkipThisStep.setOnClickListener { viewModel.skipThisStep() }
            setCalendarEditText(etDateOfBirthday)
            addPhoto(fabInfoAddPhoto)
            toolbar.withActionButton(fromDictionary(R.string.reg_info_next)) { launchCoroutineUI { processProfile(getViewSuspend())
            getUserDetails()} }
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
    }

    private fun getUserDetails() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun processProfile(view: View) {


        val email = view.etYourEmail.text.toString()
        val phone = view.etPhoneNumber.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotBlank()) {
            view.etYourEmail.error = fromDictionary(R.string.email_is_not_valid)
            return
        }
        if (!PATTERN_PHONE.matcher(phone).matches() && phone.isNotEmpty()) {
            view.etPhoneNumber.error = fromDictionary(R.string.phone_is_not_valid)
            return
        }
        viewModel.processAccount(accountModel,
                view.etPhoneNumber.text.toString(),
                view.containerSelectOccupation.getAllAbilities(),
                view.etDateOfBirthday.text.toString(),
                view.etYourEmail.isChosen,
                birthday,
                view.etPhoneNumber.isChosen,
                view.etYourEmail.text.toString(),
                view.rInfoBtnMale.isChecked
        )
    }

    override fun photoResult(uri: Uri, view: View) {
        view.ivUserAvatar?.avatarSquare(uri)
        viewModel.saveLocallyAvatarUri(uri)
    }

    private fun setupViews(view: View) {
        with(view) {
            tilDateOfBirthday.hint = fromDictionary(R.string.reg_person_info_birthday)
            tilPhoneNumber.hint = fromDictionary(R.string.reg_info_phone_number)
            tvInfoGender.text = fromDictionary(R.string.reg_person_info_gender)
            rInfoBtnMale.text = fromDictionary(R.string.reg_person_info_male_gender)
            rInfoBtnFemale.text = fromDictionary(R.string.reg_person_info_female_gender)
            tilYourEmail.hint = fromDictionary(R.string.reg_info_email)
            tvSkipThisStep.text = fromDictionary(R.string.reg_info_skip)
        }
    }

    companion object {

        fun newInstance(account: ProfileAccountModel): PersonalInfoController {
            val params = Bundle()
            params.putSerializable(EXTRA_PROFILE, account)
            return PersonalInfoController(params)
        }
    }
}