package com.mnassa.screen

import com.bluelinelabs.conductor.Controller

/**
 * Created by Peter on 3/12/2018.
 */
interface MnassaRouter {
    fun open(self: Controller, controller: Controller)
    fun close(self: Controller)
}