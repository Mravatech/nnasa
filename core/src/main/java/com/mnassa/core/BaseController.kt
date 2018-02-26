package com.mnassa.core

import android.arch.lifecycle.Lifecycle
import com.mnassa.core.events.CompositeEventListener

/**
 * Created by Peter on 2/20/2018.
 */
interface BaseController<out VM : BaseViewModel> {
    val viewModel: VM

    val lifecycle: CompositeEventListener<Lifecycle.Event>
}