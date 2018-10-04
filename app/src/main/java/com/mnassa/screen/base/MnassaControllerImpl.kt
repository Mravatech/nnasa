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
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.NetworkInteractor
import com.mnassa.domain.interactor.SettingsInteractor
import com.mnassa.domain.model.LogoutReason
import com.mnassa.extensions.hideKeyboard
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.MnassaRouter
import com.mnassa.translation.fromDictionary
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.KodeinTrigger
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.lang.ref.WeakReference

/**
 * Created by Peter on 2/20/2018.
 */
abstract class MnassaControllerImpl<VM : MnassaViewModel> : BaseControllerImpl<VM>, MnassaController<VM>, KodeinAware, LayoutContainer {
    override val kodeinTrigger = KodeinTrigger()
    override val kodein: Kodein = Kodein.lazy {
        val parentKodein by closestKodein(requireNotNull(applicationContext))
        extend(parentKodein, allowOverride = true)
    }
    private val settingsInteractor: SettingsInteractor by instance()
    private val loginInteractor: LoginInteractor by instance()
    private val networkInteractor: NetworkInteractor by instance()
    private val dialogHelper: DialogHelper by instance()
    private var serverMaintenanceDialog = WeakReference<Dialog>(null)
    private var apiNotSupportedDialog = WeakReference<Dialog>(null)

    override val containerView: View? get() = view

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
        subscribeToServerMaintenanceStatus()
        subscribeToSupportedApiStatus()
    }

    override fun onDestroyView(view: View) {
        hideProgress() //prevent showing progress after screen change
        hideKeyboard()
        clearFindViewByIdCache()
        super.onDestroyView(view)
    }

    protected open fun subscribeToErrorEvents() {
        launchCoroutineUI {
            viewModel.errorMessageChannel.consumeEach {
                val context = view?.context ?: return@consumeEach
                AlertDialog.Builder(context)
                        .setTitle(fromDictionary(R.string.error_dialog_title))
                        .setMessage(it)
                        .setPositiveButton(context.getString(android.R.string.ok)) { _, _ -> }
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

    protected open fun subscribeToServerMaintenanceStatus() {
        val isMostParentController = parentController == null
        if (!isMostParentController) return

        launchCoroutineUI {
            settingsInteractor.getMaintenanceServerStatus().consumeEach { isUnderMaintenance ->
                closeServerMaintenanceDialog()
                if (isUnderMaintenance) {
                    val dialog = dialogHelper.showServerIsUnderMaintenanceDialog(getViewSuspend().context) {
                        launchCoroutineUI { loginInteractor.signOut(LogoutReason.ManualLogout()) }
                    }
                    serverMaintenanceDialog = WeakReference(dialog)
                }
            }
        }.invokeOnCompletion {
            closeServerMaintenanceDialog()
        }
    }

    protected open fun subscribeToSupportedApiStatus() {
        val isMostParentController = parentController == null
        if (!isMostParentController) return

        launchCoroutineUI {
            networkInteractor.isApiSupported().consumeEach { isSupported ->
                closeApiNotSupportedDialog()
                if (!isSupported) {
                    val dialog = dialogHelper.showUpdateAppDialog(getViewSuspend().context)
                    apiNotSupportedDialog = WeakReference(dialog)
                }
            }
        }.invokeOnCompletion {
            closeApiNotSupportedDialog()
        }
    }

    protected fun closeServerMaintenanceDialog() {
        serverMaintenanceDialog.get()?.dismiss()
        serverMaintenanceDialog.clear()
    }

    protected fun closeApiNotSupportedDialog() {
        apiNotSupportedDialog.get()?.dismiss()
        apiNotSupportedDialog.clear()
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
