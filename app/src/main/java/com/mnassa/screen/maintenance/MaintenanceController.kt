package com.mnassa.screen.maintenance

import android.view.View
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_maintenance.view.*
import org.kodein.di.generic.instance

/**
 * Created by Peter on 2/20/2018.
 */
class MaintenanceController : MnassaControllerImpl<MaintenanceViewModel>() {
    override val layoutId: Int = R.layout.controller_maintenance
    override val viewModel: MaintenanceViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        view.tvMaintenanceMessage.text = fromDictionary(R.string.server_maintenance_error)
    }

    companion object {
        fun newInstance() = MaintenanceController()
    }
}