package com.mnassa.screen.group.select

import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.GroupModel
import com.mnassa.extensions.isInvisible
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.group.list.adapters.AllGroupsRecyclerViewAdapter
import kotlinx.android.synthetic.main.controller_group_select.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/23/2018.
 */
class SelectGroupController(args: Bundle) : MnassaControllerImpl<SelectGroupViewModel>(args) {
    override val layoutId: Int = R.layout.controller_group_select
    override val viewModel: SelectGroupViewModel by instance(arg = getParams(args))
    private val adapter = AllGroupsRecyclerViewAdapter(withGroupOptions = false, withHeader = false)

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        val listener = targetController as OnGroupSelectedListener

        with(view) {
            rvAllGroups.adapter = adapter
        }

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.groupChannel.consumeEach {
                adapter.setGroups(it)
                adapter.isLoadingEnabled = false

                view.rvAllGroups.isInvisible = it.isEmpty()
                view.rlEmptyView.isInvisible = !it.isEmpty()
            }
        }

        adapter.onItemClickListener = {
            listener.onGroupSelected(it)
            close()
        }
    }

    override fun onDestroyView(view: View) {
        view.rvAllGroups.adapter = null
        adapter.destroyCallbacks()
        super.onDestroyView(view)
    }

    interface OnGroupSelectedListener {
        fun onGroupSelected(group: GroupModel)
    }

    companion object {
        private const val EXTRA_ONLY_ADMIN = "EXTRA_ONLY_ADMIN"

        fun <T> newInstance(listener: T, onlyAdmin: Boolean): SelectGroupController where T : Controller, T : OnGroupSelectedListener {
            val args = Bundle()
            args.putBoolean(EXTRA_ONLY_ADMIN, onlyAdmin)

            val controller = SelectGroupController(args)
            controller.targetController = listener
            return controller
        }

        fun getParams(args: Bundle) = SelectGroupViewModel.Params(args.getBoolean(EXTRA_ONLY_ADMIN))
    }
}