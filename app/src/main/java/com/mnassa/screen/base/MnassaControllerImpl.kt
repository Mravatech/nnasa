package com.mnassa.screen.base

import android.app.Dialog
import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.mnassa.R
import com.mnassa.core.BaseControllerImpl
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.core.events.awaitFirst
import com.mnassa.extensions.hideKeyboard
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.MnassaRouter
import com.mnassa.translation.fromDictionary
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.KodeinTrigger
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

/**
 * Created by Peter on 2/20/2018.
 */
abstract class MnassaControllerImpl<VM : MnassaViewModel> : BaseControllerImpl<VM>, MnassaController<VM>, KodeinAware {
    override val kodeinTrigger = KodeinTrigger()
    override val kodein: Kodein = Kodein.lazy {
        val parentKodein by closestKodein(requireNotNull(applicationContext))
        extend(parentKodein, allowOverride = true)
    }

    constructor(params: Bundle) : super(params)
    constructor() : super()


    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        kodeinTrigger.trigger()
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        subscribeToProgressEvents()
        subscribeToErrorEvents()
    }

    override fun onViewDestroyed(view: View) {
        hideProgress() //prevent showing progress after screen change
        hideKeyboard()
        super.onViewDestroyed(view)
    }

    protected open fun subscribeToErrorEvents() {
        launchCoroutineUI {
            viewModel.errorMessageChannel.consumeEach {
                val context = view?.context ?: return@consumeEach
                AlertDialog.Builder(context)
                        .setTitle(fromDictionary(R.string.error_dialog_title))
                        .setMessage(it)
                        .setPositiveButton(context.getString(android.R.string.ok), { _, _ -> })
                        .show()
            }
        }
    }

    protected open fun subscribeToProgressEvents() {
        launchCoroutineUI {
            viewModel.isProgressEnabledChannel.consumeEach { isProgressEnabled ->
                if (isProgressEnabled) {
                    showProgress()
                } else {
                    hideProgress()
                }
            }
        }
    }

    private var progressDialog: Dialog? = null
    protected fun showProgress() {
        hideKeyboard()
        if (progressDialog != null) return
        progressDialog?.dismiss()
        val dialogHelper: DialogHelper by instance()
        progressDialog = dialogHelper.showProgressDialog(requireNotNull(view).context)
    }

    protected fun hideProgress() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    protected fun open(controller: Controller) = mnassaRouter.open(this, controller)
    protected fun close() = mnassaRouter.close(this)

    protected val mnassaRouter: MnassaRouter
        get() {
            val parentController = parentController
            val activity = activity

            return when {
                parentController is MnassaRouter -> parentController
                activity is MnassaRouter -> activity
                else -> throw IllegalStateException("Mnassa router not found for $this")
            }
        }

    protected suspend fun getViewSuspend(): View {
        val localView = view
        if (localView != null) return localView
        lifecycle.awaitFirst { it.ordinal >= Lifecycle.Event.ON_START.ordinal && it.ordinal < Lifecycle.Event.ON_STOP.ordinal }
        return requireNotNull(view)
    }
}
