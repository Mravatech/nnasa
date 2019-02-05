package com.mnassa.core

import androidx.lifecycle.Lifecycle
import com.mnassa.core.events.CompositeEventListener
import com.mnassa.core.events.OnActivityResultEvent
import com.mnassa.core.permissions.PermissionsManager

/**
 * Created by Peter on 2/20/2018.
 */
interface BaseController<out VM : BaseViewModel> {
    val viewModel: VM

    val lifecycle: CompositeEventListener<Lifecycle.Event>
    val onActivityResult: CompositeEventListener<OnActivityResultEvent>
    val permissions: PermissionsManager
}