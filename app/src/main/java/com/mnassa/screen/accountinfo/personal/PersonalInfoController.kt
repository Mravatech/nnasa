package com.mnassa.screen.accountinfo.personal

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.PATTERN_PHONE_TAIL
import com.mnassa.extensions.avatarSquare
import com.mnassa.screen.buildnetwork.BuildNetworkController
import com.mnassa.screen.profile.edit.BaseEditableProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_personal_info.view.*
import kotlinx.android.synthetic.main.sub_personal_info.view.*
import kotlinx.android.synthetic.main.sub_profile_avatar.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

class PersonalInfoController(data: Bundle) : BaseEditableProfileController<PersonalInfoViewModel>(data) {

    override val layoutId: Int = R.layout.controller_personal_info
    override val viewModel: PersonalInfoViewModel by instance()

    private val accountModel: ShortAccountModel by lazy { args.getSerializable(EXTRA_ACCOUNT) as ShortAccountModel }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        setupViews(view)
        view.etPhoneNumber.setText(accountModel.contactPhone?.replace("+", ""))
        view.etPhoneNumber.setHideMode(false)
        view.etYourEmail.setHideMode(false)
        view.tvSkipThisStep.setOnClickListener { viewModel.skipThisStep() }
        setCalendarEditText(view.etDateOfBirthday)
        addPhoto(view.fabInfoAddPhoto)
        view.btnHeaderNext.setOnClickListener { proccesProfile(view) }
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

    override fun proccesProfile(view: View) {
        val email = view.etYourEmail.text.toString()
        val phone = view.etPhoneNumber.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotBlank()) {
            view.etYourEmail.error = fromDictionary(R.string.email_is_not_valid)
            return
        }
        if (!PATTERN_PHONE_TAIL.matcher(phone).matches() && phone.isNotEmpty()) {
            view.etPhoneNumber.error = fromDictionary(R.string.phone_is_not_valid)
            return
        }
        viewModel.processAccount(accountModel,
                view.etPhoneNumber.text.toString(),
                view.containerSelectOccupation.getAllAbilities(),
                view.etDateOfBirthday.text.toString(),
                view.etYourEmail.isChosen,
                timeMillis,
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
        view.tilDateOfBirthday.hint = fromDictionary(R.string.reg_person_info_birthday)
        view.tilPhoneNumber.hint = fromDictionary(R.string.reg_info_phone_number)
        view.tvInfoGender.text = fromDictionary(R.string.reg_person_info_gender)
        view.rInfoBtnMale.text = fromDictionary(R.string.reg_person_info_male_gender)
        view.rInfoBtnFemale.text = fromDictionary(R.string.reg_person_info_female_gender)
        view.tilYourEmail.hint = fromDictionary(R.string.reg_info_email)
        view.tvSkipThisStep.text = fromDictionary(R.string.reg_info_skip)
        view.tvHeader.text = fromDictionary(R.string.reg_personal_info_title)
        view.btnHeaderNext.text = fromDictionary(R.string.reg_info_next)
    }

    companion object {
        private const val EXTRA_ACCOUNT = "EXTRA_ACCOUNT"

        fun newInstance(account: ShortAccountModel): PersonalInfoController {
            val params = Bundle()
            params.putSerializable(EXTRA_ACCOUNT, account)
            return PersonalInfoController(params)
        }
    }
}