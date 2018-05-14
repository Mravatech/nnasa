package com.mnassa.screen.invite

import android.Manifest
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.AdapterView
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.impl.PhoneContactImpl
import com.mnassa.extensions.PATTERN_PHONE_TAIL
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.openApplicationSettings
import com.mnassa.helper.CountryHelper
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.IntentHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.invite.history.HistoryController
import com.mnassa.screen.login.enterphone.CountryCode
import com.mnassa.screen.login.enterphone.CountryCodeAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_invite_to_mnassa.view.*
import kotlinx.android.synthetic.main.header_main.view.*
import kotlinx.android.synthetic.main.phone_input.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/19/2018
 */

class InviteController : MnassaControllerImpl<InviteViewModel>() {
    override val layoutId = R.layout.controller_invite_to_mnassa
    override val viewModel: InviteViewModel by instance()
    private var adapter: InviteAdapter = InviteAdapter()
    private val dialog: DialogHelper by instance()
    private val countryHelper: CountryHelper by instance()
    private val intentHelper: IntentHelper by instance()

    private var countryCodePhrase = ""
    private val phoneNumber: String
        get() {
            val view = view ?: return EMPTY_STRING
            val countryCode = view.spinnerPhoneCode.selectedItem as? CountryCode
                    ?: return EMPTY_STRING
            countryCodePhrase = countryCode.phonePrefix.code.replace("+", EMPTY_STRING)
            return countryCodePhrase + view.etPhoneNumberTail.text.toString()
        }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        initViews(view)
        checkReadContactPermission(view)
        launchCoroutineUI {
            viewModel.checkPhoneContactChannel.consumeEach {
                if (it) {
                    val packageManager = activity?.packageManager
                    val intent = intentHelper.getWhatsAppIntent(EMPTY_STRING, EMPTY_STRING)
                    val name = adapter.getNameByNumber(view.etPhoneNumberTail.text.toString())
                    dialog.chooseSendInviteWith(view.context, name, intent.resolveActivity(packageManager) != null,
                            { inviteWith -> handleInviteWith(inviteWith, phoneNumber) })
                }
            }
        }
        launchCoroutineUI {
            viewModel.invitesCountChannel.consumeEach {
                view.toolbar.title = fromDictionary(R.string.invite_invite_invites_left).format(it)
            }
        }
        adapter.onItemClickListener = {
            view.btnInvite.background = ContextCompat.getDrawable(view.context, R.drawable.button_invite_to_mnassa_enabled_background)
            var number = it.phoneNumber
            if (number.startsWith(countryCodePhrase)) {
                number = number.replaceFirst(countryCodePhrase, "")
            }
            view.etPhoneNumberTail.setText(number)
        }
    }

    override fun onDestroyView(view: View) {
        view.rvInviteToMnassa.adapter = null
        super.onDestroyView(view)
    }

    private fun checkReadContactPermission(view: View) {
        launchCoroutineUI {
            val permissionsResult = permissions.requestPermissions(Manifest.permission.READ_CONTACTS)
            if (permissionsResult.isAllGranted) {
                viewModel.retrievePhoneContacts()
                viewModel.phoneContactChannel.consumeEach {
                    view.rvInviteToMnassa.layoutManager = LinearLayoutManager(view.context)
                    adapter.setData(it)
                    view.rvInviteToMnassa.adapter = adapter
                }
            } else {
                Snackbar.make(view, fromDictionary(R.string.tab_connections_contact_permissions_description), Snackbar.LENGTH_INDEFINITE)
                        .setAction(fromDictionary(R.string.tab_connections_contact_permissions_button)) {
                            view.context.openApplicationSettings()
                        }.show()
            }
        }
    }

    private fun initViews(view: View) {
        with(view) {
            tvEnterTextSuggest.text = fromDictionary(R.string.invite_text_suggest)
            toolbar.title = fromDictionary(R.string.invite_invite_header)
            etInviteSearch.hint = fromDictionary(R.string.invite_search_hint)
            etPhoneNumberTail.hint = fromDictionary(R.string.invite_phone_number_hint)
            btnInvite.text = fromDictionary(R.string.invite_invite_button_text)
            spinnerPhoneCode.adapter = CountryCodeAdapter(spinnerPhoneCode.context, countryHelper.countries)
            spinnerPhoneCode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = onInputChanged()
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = onInputChanged()
            }
            etPhoneNumberTail.addTextChangedListener(SimpleTextWatcher {
                view.btnInvite.isEnabled = validateInput()
                adapter.searchByNumber(it)
            })
            btnInvite.setOnClickListener {
                viewModel.checkPhoneContact(PhoneContactImpl(
                        phoneNumber,
                        adapter.getNameByNumber(etPhoneNumberTail.text.toString())
                                ?: EMPTY_STRING, null))
            }
            etInviteSearch.addTextChangedListener(
                    SimpleTextWatcher { searchWord ->
                        adapter.searchByName(searchWord)
                    }
            )
            toolbar.ivToolbarMore.setImageResource(R.drawable.ic_archive)
            toolbar.onMoreClickListener = {
                open(HistoryController.newInstance())
            }
        }
    }

    private fun validateInput(): Boolean {
        return PATTERN_PHONE_TAIL.matcher(phoneNumber).matches()
    }

    private fun onInputChanged() {
        with(view ?: return) {
            btnInvite.isEnabled = validateInput()
            etPhoneNumberTail.error = null
            adapter.searchByNumber(countryCodePhrase + etPhoneNumberTail.text.toString())
        }
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
        val intent = intentHelper.getWhatsAppIntent(fromDictionary(R.string.invite_invite_massage_whats_app), number)
        if (intent.resolveActivity(packageManager) != null) {
            activity?.startActivity(intent)
        }
    }

    private fun sendWithSMS(number: String) {
        val intent = intentHelper.getSMSIntent(fromDictionary(R.string.invite_invite_massage_email), number)
        startActivity(intent)
    }

    private fun shareInvite() {
        val intent = intentHelper.getShareIntent(fromDictionary(R.string.invite_invite_massage_email))
        startActivity(intent)
    }

    companion object {
        const val EMPTY_STRING = ""
        const val INVITE_WITH_WHATS_APP = 1
        const val INVITE_WITH_SMS = 2
        const val INVITE_WITH_SHARE = 3
        fun newInstance() = InviteController()
    }
}