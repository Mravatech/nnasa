package com.mnassa.screen.accountinfo.organization

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
import kotlinx.android.synthetic.main.controller_organization_info.view.*
import kotlinx.android.synthetic.main.sub_company_info.view.*
import kotlinx.android.synthetic.main.sub_profile_avatar.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

class OrganizationInfoController(data: Bundle) : BaseEditableProfileController<OrganizationInfoViewModel>(data) {

    override val layoutId: Int = R.layout.controller_organization_info
    override val viewModel: OrganizationInfoViewModel by instance()
    private val accountModel: ShortAccountModel by lazy { args.getSerializable(EXTRA_ACCOUNT) as ShortAccountModel }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        setupViews(view)
        with(view){
            setCalendarEditText(etFoundation)
            addPhoto(fabInfoAddPhoto)
            etCompanyEmail.setHideMode(false)
            etCompanyPhone.setHideMode(false)
            etCompanyPhone.setText(accountModel.contactPhone)
            etCompanyNameNotEditable.setText(accountModel.organizationInfo?.organizationName)
            btnHeaderNext.setOnClickListener { proccesProfile(this) }
            tvSkipThisStep.setOnClickListener {
                viewModel.skipThisStep()
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

    override fun photoResult(uri: Uri, view: View) {
        view.ivUserAvatar?.avatarSquare(uri)
        viewModel.saveLocallyAvatarUri(uri)
    }

    override fun proccesProfile(view: View) {
        val email = view.etCompanyEmail.text.toString()
        val phone = view.etCompanyPhone.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotEmpty()) {
            view.etCompanyEmail.error = fromDictionary(R.string.email_is_not_valid)
            return
        }
        if (!PATTERN_PHONE_TAIL.matcher(phone).matches() && phone.isNotEmpty()) {
            view.etCompanyPhone.error = fromDictionary(R.string.phone_is_not_valid)
            return
        }
        viewModel.processAccount(accountModel,
                view.vCompanyStatus.getOrganizationType(),
                view.etFoundation.text.toString().takeIf { it.isNotBlank() },
                view.etCompanyEmail.isChosen,
                view.etCompanyPhone.isChosen,
                birthday,
                view.etCompanyEmail.text.toString().takeIf { it.isNotBlank() },
                view.etCompanyPhone.text.toString().takeIf { it.isNotBlank() },
                view.etWebSite.text.toString().takeIf { it.isNotBlank() }
        )
    }

    private fun setupViews(view: View) {
        with(view){
            tilCompanyPhone.hint = fromDictionary(R.string.reg_info_phone_number)
            tilCompanyNameNotEditable.hint = fromDictionary(R.string.reg_company_name)
            tvHeader.text = fromDictionary(R.string.reg_company_title)
            btnHeaderNext.text = fromDictionary(R.string.reg_info_next)
            tilWebSite.hint = fromDictionary(R.string.reg_company_website)
            tilCompanyEmail.hint = fromDictionary(R.string.reg_info_email)
            tilFoundation.hint = fromDictionary(R.string.reg_company_founded)
            tvSkipThisStep.text = fromDictionary(R.string.reg_info_skip)
        }
    }

    companion object {
        private const val EXTRA_ACCOUNT = "EXTRA_ACCOUNT"

        fun newInstance(account: ShortAccountModel): OrganizationInfoController {
            val params = Bundle()
            params.putSerializable(EXTRA_ACCOUNT, account)
            return OrganizationInfoController(params)
        }
    }
}