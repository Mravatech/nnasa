package com.mnassa.screen.invite

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_invite.view.*
import kotlinx.android.synthetic.main.header_login.view.*

/**
 * Created by Peter on 3/5/2018.
 */
class InviteController(args: Bundle) : MnassaControllerImpl<InviteViewModel>(args) {
    override val layoutId: Int = R.layout.controller_invite
    override val viewModel: InviteViewModel by instance()
    private val adapter = InviteAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with (view) {
            tvScreenHeader.text = fromDictionary(R.string.invite_title)
            tvInviteUsersToBuildNetwork.text = fromDictionary(R.string.invite_description)

            rvInvite.layoutManager = LinearLayoutManager(view.context)
            rvInvite.adapter = adapter

            //connections - recommended
            //occupations separator
        }


    }

    companion object {
        fun newInstance(): InviteController {
            val args = Bundle()
            return InviteController(args)
        }
    }
}