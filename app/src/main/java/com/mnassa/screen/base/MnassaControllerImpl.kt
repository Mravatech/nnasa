package com.mnassa.screen.base

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.annotation.UiThread
import androidx.lifecycle.Lifecycle
import com.afollestad.materialdialogs.MaterialDialog
import com.bluelinelabs.conductor.Controller
import com.mnassa.R
import com.mnassa.core.BaseControllerImpl
import com.mnassa.core.addons.launchUI
import com.mnassa.core.events.awaitFirst
import com.mnassa.core.errorMessagesLive
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.NetworkInteractor
import com.mnassa.domain.interactor.SettingsInteractor
import com.mnassa.core.live.consume
import com.mnassa.extensions.hideKeyboard
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.MnassaRouter
import com.mnassa.translation.fromDictionary
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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
        subscribeToGeneralErrorEvents()
        subscribeToSetupErrorEvents()
        subscribeToSupportedApiStatus()
    }

    override fun onDestroyView(view: View) {
        hideProgress() //prevent showing progress after screen change
        hideKeyboard()
        clearFindViewByIdCache()
        super.onDestroyView(view)
    }

    protected open fun subscribeToGeneralErrorEvents() {
        launchUI {
            errorMessagesLive.consume {
                if (isAttached && view?.context != null) {
                    launch(Dispatchers.Main) {
                        showGeneralErrorMessage(it)
                    }
                    // Don't let the event to pass to
                    // the other observers.
                    return@consume true
                } else {
                    return@consume false
                }
            }
        }
    }

    protected open fun showGeneralErrorMessage(message: String) {
        val context = view?.context ?: return
        MaterialDialog.Builder(context)
            .title(fromDictionary(R.string.error_dialog_title))
            .content(message)
            .positiveText(android.R.string.ok)
            .show()
    }

    protected open fun subscribeToSetupErrorEvents() {
        launchUI {
            viewModel.setupErrorChannel.consumeEach {
                // TODO: Show an error that blocks the interface and prompts user
                // to reload the viewmodel.
                showGeneralErrorMessage(it)
            }
        }
    }

    protected open fun subscribeToProgressEvents() {
        launchUI {
            viewModel.isProgressEnabledChannel.consumeEach { event ->
                if (event is ShowProgressEvent) {
                    showProgress(event.hideKeyboard)
                } else {
                    hideProgress()
                }
            }
        }
    }

    protected open fun subscribeToSupportedApiStatus() {
        val isMostParentController = parentController == null
        if (!isMostParentController) return

        launchUI {
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

    protected fun closeApiNotSupportedDialog() {
        apiNotSupportedDialog.get()?.dismiss()
        apiNotSupportedDialog.clear()
    }

    private var progressDialog: Dialog? = null
    protected fun showProgress(hideKeyboard: Boolean) {
        if (hideKeyboard) {
            hideKeyboard()
        }

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

    @UiThread
    protected suspend fun getViewSuspend(): View =
        view ?: run {
            // Wait for the controller to
            // start.
            lifecycle.awaitFirst {
                it.ordinal >= Lifecycle.Event.ON_START.ordinal &&
                    it.ordinal < Lifecycle.Event.ON_STOP.ordinal
            }

            view!!
        }
}
