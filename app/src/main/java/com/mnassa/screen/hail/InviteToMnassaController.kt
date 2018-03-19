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
        view.tvCodeOperator.text = "+380"
        view.tvCodeOperator.setOnClickListener {
            dialog.chooseCountryInvite(view.context, {
                view.tvCodeOperator.text = it
            })
        }
        view.etInvitePhoneNumber.addTextChangedListener(SimpleTextWatcher {
            if (it.length == 9) {
                view.btnInvite.background = ContextCompat.getDrawable(view.context, R.drawable.button_invite_to_mnassa_selected_background)
            } else {
                view.btnInvite.background = ContextCompat.getDrawable(view.context, R.drawable.button_invite_to_mnassa_background)
            }
        })
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
                view.btnInvite.background = ContextCompat.getDrawable(view.context, R.drawable.button_invite_to_mnassa_selected_background)
                view.etInvitePhoneNumber.setText(it.phoneNumber)
            }
        }
        view.etInviteSearch.addTextChangedListener(
                SimpleTextWatcher { searchWord ->
                    adapter?.search(searchWord)
                }
        )
    }

    companion object {
        fun newInstance() = InviteToMnassaController()
    }
}