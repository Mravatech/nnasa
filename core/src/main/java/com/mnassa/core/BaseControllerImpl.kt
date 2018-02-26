package com.mnassa.core

import android.arch.lifecycle.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.archlifecycle.ControllerLifecycleRegistryOwner
import com.mnassa.core.addons.SubscriptionContainer
import com.mnassa.core.addons.SubscriptionsContainerDelegate
import com.mnassa.core.events.CompositeEventListener
import com.mnassa.core.events.EmitableCompositeEventListener
import com.mnassa.core.events.impl.SimpleCompositeEventListener

/**
 * Created by Peter on 2/20/2018.
 */

@Suppress("LeakingThis", "DEPRECATION")
abstract class BaseControllerImpl<VM : BaseViewModel>(args: Bundle)
    : Controller(args),
        BaseController<VM>,
        LifecycleRegistryOwner,
        SubscriptionContainer by SubscriptionsContainerDelegate() {

    constructor() : this(Bundle())

    /**
     * Provides xml layout id
     * */
    abstract val layoutId: Int
    abstract override val viewModel: VM
    private var isViewModelInitialized = false


    //Lifecycle
    private val lifecycleRegistryOwner = ControllerLifecycleRegistryOwner(this)
    private val lifecycleEmitter: EmitableCompositeEventListener<Lifecycle.Event> = SimpleCompositeEventListener()
    override val lifecycle: CompositeEventListener<Lifecycle.Event> = lifecycleEmitter
    override fun getLifecycle(): LifecycleRegistry = lifecycleRegistryOwner.lifecycle

    init {
        addLifecycleListener(object : LifecycleListener() {

            private var savedInstanceState: Bundle? = null
            override fun preCreateView(controller: Controller) {
                if (!isViewModelInitialized) {
                    onCreated(savedInstanceState)
                    savedInstanceState = null
                    isViewModelInitialized = true
                }
            }

            override fun postCreateView(controller: Controller, view: View) {
                onViewCreated(view)
            }

            override fun preDestroyView(controller: Controller, view: View) {
                cancelAllSubscriptions() //clear View subscriptions
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

    /**
     * Invokes once after [BaseControllerImpl] creation, when it has [getApplicationContext]
     */
    open fun onCreated(savedInstanceState: Bundle?) {
        viewModel.onCreate(savedInstanceState)
    }

    open fun onViewCreated(view: View) {
    }

    open fun onViewDestroyed(view: View) {
    }
}