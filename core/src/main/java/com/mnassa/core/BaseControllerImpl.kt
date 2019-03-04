package com.mnassa.core

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.GenericLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LifecycleRegistryOwner
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.archlifecycle.ControllerLifecycleOwner
import com.mnassa.core.addons.SubscriptionContainer
import com.mnassa.core.addons.SubscriptionsContainerDelegate
import com.mnassa.core.events.CompositeEventListener
import com.mnassa.core.events.EmitableCompositeEventListener
import com.mnassa.core.events.OnActivityResultEvent
import com.mnassa.core.events.impl.SimpleCompositeEventListener
import com.mnassa.core.permissions.OnRequestPermissionsResultEvent
import com.mnassa.core.permissions.PermissionsManager
import com.mnassa.core.permissions.PermissionsManagerDelegate

/**
 * Created by Peter on 2/20/2018.
 */

@Suppress("LeakingThis", "DEPRECATION")
abstract class BaseControllerImpl<VM : BaseViewModel>(args: Bundle = Bundle()) : Controller(args),
    BaseController<VM>,
    LifecycleRegistryOwner,
    SubscriptionContainer by SubscriptionsContainerDelegate() {

    /**
     * Provides xml layout id
     * */
    abstract val layoutId: Int
    abstract override val viewModel: VM
    private var isViewModelInitialized = false


    //Lifecycle
    private val lifecycleRegistryOwner = ControllerLifecycleOwner(this)
    private val lifecycleEmitter: EmitableCompositeEventListener<Lifecycle.Event> = SimpleCompositeEventListener()
    override val lifecycle: CompositeEventListener<Lifecycle.Event> = lifecycleEmitter
    override fun getLifecycle(): LifecycleRegistry = lifecycleRegistryOwner.lifecycle as LifecycleRegistry

    //OnActivityResult
    private val onActivityResultEmitter: EmitableCompositeEventListener<OnActivityResultEvent> = SimpleCompositeEventListener()
    override val onActivityResult: CompositeEventListener<OnActivityResultEvent> = onActivityResultEmitter

    //OnRequestPermissionsResult
    private val onRequestPermissionResultEmitter: EmitableCompositeEventListener<OnRequestPermissionsResultEvent> = SimpleCompositeEventListener()
    override val permissions: PermissionsManager by lazy {
        PermissionsManagerDelegate(onRequestPermissionResultEmitter, view = { this })
    }

    init {
        addLifecycleListener(object : LifecycleListener() {

            private var savedInstanceState: Bundle? = null
            override fun preCreateView(controller: Controller) {
                if (!isViewModelInitialized) {
                    onCreated(savedInstanceState)
                    isViewModelInitialized = true
                }
            }

            override fun postCreateView(controller: Controller, view: View) {
                onViewCreated(view)
            }

            override fun preDestroyView(controller: Controller, view: View) {
                onViewDestroyed(view)
            }

            override fun preDestroy(controller: Controller) {
                viewModel.onCleared()
            }

            override fun onSaveInstanceState(controller: Controller, outState: Bundle) {
                viewModel.saveInstanceState(outState)
            }

            override fun onRestoreInstanceState(controller: Controller, savedInstanceState: Bundle) {
                this.savedInstanceState = savedInstanceState
            }
        })

        lifecycleRegistryOwner.lifecycle.addObserver(GenericLifecycleObserver { _, event -> lifecycleEmitter.emit(event)})
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View =
            inflater.inflate(layoutId, container, false)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultEmitter.emit(OnActivityResultEvent(requestCode, resultCode, data))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val shouldShowRequestPermissionRationale = BooleanArray(permissions.size) {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(permissions[it])
        }

        onRequestPermissionResultEmitter.emit(OnRequestPermissionsResultEvent(
                requestCode,
                permissions.toList(),
                grantResults.toList(),
                shouldShowRequestPermissionRationale.toList()))
    }

    /**
     * Invokes once after [BaseControllerImpl] creation,
     * when it has [getApplicationContext]
     */
    open fun onCreated(savedInstanceState: Bundle?) {
        viewModel.onCreate(savedInstanceState)
        openSubscriptionsScope()
    }

    open fun onViewCreated(view: View) {
    }

    open fun onViewDestroyed(view: View) {
    }

    override fun onDestroy() {
        closeSubscriptionsScope()
        super.onDestroy()
    }
}