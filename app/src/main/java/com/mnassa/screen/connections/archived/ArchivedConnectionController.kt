package com.mnassa.screen.connections.archived

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_archived.view.*
import kotlinx.android.synthetic.main.header_main.view.*

/**
 * Created by Peter on 9.03.2018.
 */
class ArchivedConnectionController : MnassaControllerImpl<ArchivedConnectionViewModel>() {
    override val layoutId: Int = R.layout.controller_archived
    override val viewModel: ArchivedConnectionViewModel by instance()
    private val adapter = ArchivedConnectionsRVAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view

        with(view) {
            tvScreenHeader.text = fromDictionary(R.string.archived_connections_title)

            rvArchivedConnection.layoutManager = LinearLayoutManager(context)
            rvArchivedConnection.adapter = adapter


        }
    }

    companion object {
        fun newInstance() = ArchivedConnectionController()
    }
}