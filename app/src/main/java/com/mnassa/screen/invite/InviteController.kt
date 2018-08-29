package com.mnassa.screen.invite

import android.Manifest
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.AdapterView
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.impl.PhoneContactImpl
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.formattedText
import com.mnassa.extensions.openApplicationSettings
import com.mnassa.helper.CountryHelper
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.IntentHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.invite.history.HistoryController
import com.mnassa.screen.login.enterphone.CountryCode
import com.mnassa.screen.login.enterphone.CountryCodeAdapter
import com.mnassa.screen.login.enterphone.PhoneNumber
import com.mnassa.screen.login.enterphone.withTail
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_invite_to_mnassa.view.*
import kotlinx.android.synthetic.main.header_main.view.*
import kotlinx.android.synthetic.main.phone_input.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance


class InviteController : MnassaControllerImpl<InviteViewModel>() {
    override val layoutId = R.layout.controller_invite_to_mnassa
    override val viewModel: InviteViewModel by instance()
    private var adapter: InviteAdapter = InviteAdapter()
    private val dialog: DialogHelper by instance()
    private val countryHelper: CountryHelper by instance()
    private val intentHelper: IntentHelper by instance()

    private val phoneNumber: String?
        get() {
            val view = view ?: return null
            val countryCode = view.spinnerPhoneCode.selectedItem as? CountryCode ?: return null
            return countryCode.withTail(view.etPhoneNumberTail.text.toString()).normalize()
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
                    dialog.chooseSendInviteWith(
                            context = view.context,
                            name = name,
                            isWhatsAppInstalled = intent.resolveActivity(packageManager) != null,
                            onInviteWithClick = { inviteWith -> handleInviteWith(inviteWith, phoneNumber ?: return@chooseSendInviteWith) }
                    )
                }
            }
        }
        launchCoroutineUI {
            viewModel.invitesCountChannel.consumeEach {
                view.toolbar.title = fromDictionary(R.string.invite_invite_invites_left).format(it)
            }
        }
        adapter.onItemClickListener = {
            val numberWithoutPlus = it.phoneNumber.replace("+", "")
            //set country code
            var suggestedCountyCode: String? = null
            for (countryIndex in 0 until countryHelper.countries.size) {
                val visibleCode = countryHelper.countries[countryIndex].phonePrefix.visibleCode.replace("+", "")
                val normalizedCode = countryHelper.countries[countryIndex].phonePrefix.normalizedCode.replace("+", "")
                if (numberWithoutPlus.startsWith(visibleCode) || numberWithoutPlus.startsWith(normalizedCode) ) {
                    view.spinnerPhoneCode.setSelection(countryIndex)
                    suggestedCountyCode = normalizedCode
                    break
                }
            }

            if (suggestedCountyCode != null) {
                view.etPhoneNumberTail.setText(numberWithoutPlus.substring(startIndex = suggestedCountyCode.length))
            } else {
                view.etPhoneNumberTail.setText(numberWithoutPlus)
            }
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
            launchCoroutineUI {
                val reward = viewModel.getInviteReward()
                tvEnterTextSuggest.text = if (reward == null) fromDictionary(R.string.invite_text_suggest)
                else fromDictionary(R.string.invite_reward).format(reward)
            }


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
                adapter.searchByNameOrNumber(it)
            })
            btnInvite.setOnClickListener {
                viewModel.checkPhoneContact(PhoneContactImpl(
                        phoneNumber ?: return@setOnClickListener,
                        adapter.getNameByNumber(etPhoneNumberTail.text.toString())
                                ?: EMPTY_STRING, null))
            }
            etInviteSearch.addTextChangedListener(
                    SimpleTextWatcher { searchWord -> adapter.searchByNameOrNumber(searchWord) }
            )
            toolbar.ivToolbarMore.setImageResource(R.drawable.ic_archive)
            toolbar.onMoreClickListener = { open(HistoryController.newInstance()) }
        }
    }

    private fun validateInput(): Boolean {
        return PhoneNumber.isValid(phoneNumber)
    }

    private fun onInputChanged() {
        with(view ?: return) {
            btnInvite.isEnabled = validateInput()
            etPhoneNumberTail.error = null
            adapter.searchByNameOrNumber(etPhoneNumberTail.text.toString())
        }
    }

    private fun handleInviteWith(inviteWith: Int, number: String) {
        val inviteSourceHolder: InviteSourceHolder by instance()
        val inviteSource = inviteSourceHolder.source
        val message = when (inviteSource) {
            is InviteSource.Post -> fromDictionary(R.string.invite_invite_from_post).format(inviteSource.post.formattedText)
            is InviteSource.Event -> fromDictionary(R.string.invite_invite_from_event).format(inviteSource.event.title)
            is InviteSource.Group -> fromDictionary(R.string.invite_invite_from_group).format(inviteSource.group.name)
            else -> if (inviteWith == INVITE_WITH_WHATS_APP) fromDictionary(R.string.invite_invite_massage_whats_app)
            else fromDictionary(R.string.invite_invite_massage_email)
        }

        when (inviteWith) {
            INVITE_WITH_WHATS_APP -> sendWithWhatsApp(number, message)
            INVITE_WITH_SMS -> sendWithSMS(number, message)
            INVITE_WITH_SHARE -> shareInvite(message)
        }
    }

    private fun sendWithWhatsApp(number: String, message: String) {
        val packageManager = activity?.packageManager
        val intent = intentHelper.getWhatsAppIntent(message, number)
        if (intent.resolveActivity(packageManager) != null) {
            activity?.startActivity(intent)
        }
    }

    private fun sendWithSMS(number: String, message: String) {
        val intent = intentHelper.getSMSIntent(message, number)
        startActivity(intent)
    }

    private fun shareInvite(message: String) {
        val intent = intentHelper.getShareIntent(message)
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