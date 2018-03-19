package com.mnassa.screen.hail

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_invite_to_mnassa.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/19/2018
 */

class InviteToMnassaController : MnassaControllerImpl<InviteToMnassaViewModel>() {
    override val layoutId = R.layout.controller_invite_to_mnassa
    override val viewModel: InviteToMnassaViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.tvEnterTextSuggest.text = fromDictionary(R.string.invite_text_suggest)
        view.tvToolbarScreenHeader.text = fromDictionary(R.string.invite_invite_header)
        view.etInviteSearch.hint = fromDictionary(R.string.invite_search_hint)
        view.etInvitePhoneNumber.hint = fromDictionary(R.string.invite_phone_number_hint)
        view.btnInvite.text = fromDictionary(R.string.invite_invite_button_text)

        view.rvInviteToMnassa.layoutManager = LinearLayoutManager(view.context)
        view.rvInviteToMnassa.adapter = InviteToMnassaAdapter(emptyList())

    }

    companion object {
        fun newInstance() = InviteToMnassaController()
    }
}