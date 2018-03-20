package com.mnassa.screen.hail

import android.Manifest
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.dialog.DialogHelper
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_invite_to_mnassa.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import android.content.Intent
import android.net.Uri
import com.mnassa.domain.model.PhoneContact
import com.mnassa.domain.model.impl.PhoneContactImpl
import java.net.URLEncoder

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/19/2018
 */

class InviteToMnassaController : MnassaControllerImpl<InviteToMnassaViewModel>() {
    override val layoutId = R.layout.controller_invite_to_mnassa
    override val viewModel: InviteToMnassaViewModel by instance()
    private var adapter: InviteToMnassaAdapter? = null
    private val dialog: DialogHelper by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.tvEnterTextSuggest.text = fromDictionary(R.string.invite_text_suggest)
        view.tvToolbarScreenHeader.text = fromDictionary(R.string.invite_invite_header)
        view.etInviteSearch.hint = fromDictionary(R.string.invite_search_hint)
        view.etInvitePhoneNumber.hint = fromDictionary(R.string.invite_phone_number_hint)
        view.btnInvite.text = fromDictionary(R.string.invite_invite_button_text)
        view.tvCodeOperator.text = fromDictionary(R.string.invite_invite_country_ua_code)
        view.tvCodeOperator.setOnClickListener {
            dialog.chooseCountryInvite(view.context, {
                view.tvCodeOperator.text = it
            })
        }
        view.etInvitePhoneNumber.addTextChangedListener(SimpleTextWatcher {
            view.btnInvite.isEnabled = it.length >= PHONE_NUMBER_WITHOUT_CODE
            adapter?.searchByNumber(it)
        })
        view.btnInvite.setOnClickListener {
            viewModel.checkPhoneContact(PhoneContactImpl(
                    view.tvCodeOperator.text.toString() + view.etInvitePhoneNumber.text.toString(),
                    adapter?.getNameByNumber(view.etInvitePhoneNumber.text.toString()) ?: "", null))
        }
        launchCoroutineUI {
            if (permissions.requestPermissions(Manifest.permission.READ_CONTACTS).isAllGranted) {
                viewModel.retrievePhoneContacts()
            }
        }
        launchCoroutineUI {
            viewModel.phoneContactChannel.consumeEach {
                adapter = InviteToMnassaAdapter(it, viewModel)
                view.rvInviteToMnassa.layoutManager = LinearLayoutManager(view.context)
                view.rvInviteToMnassa.adapter = adapter
            }
        }
        launchCoroutineUI {
            viewModel.phoneSelectedChannel.consumeEach {
                view.btnInvite.background = ContextCompat.getDrawable(view.context, R.drawable.button_invite_to_mnassa_enabled_background)
                view.etInvitePhoneNumber.setText(it.phoneNumber)
            }
        }
        launchCoroutineUI {
            viewModel.checkPhoneContactChannel.consumeEach {
                if (it) {
                    val packageManager = activity?.packageManager
                    val i = getWhatsAppIntent("")
                    val name = adapter?.getNameByNumber(view.etInvitePhoneNumber.text.toString())
                    dialog.chooseSendInviteWith(view.context, name, i.resolveActivity(packageManager) != null,
                            { inviteWith ->
                                handleInviteWith(inviteWith,
                                        view.tvCodeOperator.text.toString() + view.etInvitePhoneNumber.text.toString())
                            })
                }
            }
        }
        view.etInviteSearch.addTextChangedListener(
                SimpleTextWatcher { searchWord ->
                    adapter?.searchByName(searchWord)
                }
        )
    }

    private fun handleInviteWith(inviteWith: Int, number: String) {
        when (inviteWith) {
            INVITE_WITH_WHATS_APP -> sendWithWhatsApp(number)
            INVITE_WITH_SMS -> sendWithSMS(number)
            INVITE_WITH_SHARE -> shareInvite()
        }
    }

    private fun sendWithWhatsApp(number: String) {
        val packageManager = activity?.packageManager
        val intent = getWhatsAppIntent(number)
        if (intent.resolveActivity(packageManager) != null) {
            activity?.startActivity(intent)
        }
    }

    private fun sendWithSMS(number: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$number"))
        intent.putExtra(SMS_BODY, fromDictionary(R.string.invite_invite_massage_email))
        startActivity(intent)
    }

    private fun shareInvite() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, fromDictionary(R.string.invite_invite_massage_email))
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    private fun getWhatsAppIntent(number: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        val url = WHATS_APP_START_URI + number + WHATS_APP_MIDDLE_URI +
                URLEncoder.encode(fromDictionary(R.string.invite_invite_massage_whats_app), "UTF-8")
        intent.`package` = WHATS_APP_PACKAGE
        intent.data = Uri.parse(url)
        return intent
    }

    companion object {
        const val PHONE_NUMBER_WITHOUT_CODE = 9
        const val WHATS_APP_PACKAGE = "com.whatsapp"
        const val WHATS_APP_START_URI = "https://api.whatsapp.com/send?phone="
        const val WHATS_APP_MIDDLE_URI = "&text="
        const val SMS_BODY = "sms_body"
        const val INVITE_WITH_WHATS_APP = 1
        const val INVITE_WITH_SMS = 2
        const val INVITE_WITH_SHARE = 3
        fun newInstance() = InviteToMnassaController()
    }
}