package com.mnassa.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.bluelinelabs.conductor.Controller

/**
 * Created by Peter on 3/26/2018.
 */
class SecondActivity : MainActivity() {

    override fun createRootControllerInstance(): Controller {
        val controllerClass = intent.getSerializableExtra(EXTRA_CONTROLLER_CLASS) as Class<out Controller>
        val controllerParams = intent.getBundleExtra(EXTRA_CONTROLLER_PARAMS)

        val emptyParamConstructor = controllerClass.constructors.firstOrNull { it.parameterTypes.isEmpty() }
        val oneParamConstructor = controllerClass.constructors.firstOrNull { it.parameterTypes.size == 1 && it.parameterTypes[0] == Bundle::class.java }

        return (if (emptyParamConstructor != null) emptyParamConstructor.newInstance() else requireNotNull(oneParamConstructor).newInstance(controllerParams)) as Controller
    }

    companion object {
        private const val EXTRA_CONTROLLER_CLASS = "EXTRA_CONTROLLER_CLASS"
        private const val EXTRA_CONTROLLER_PARAMS = "EXTRA_CONTROLLER_PARAMS"

        fun start(context: Context, controllerClass: Class<out Controller>, params: Bundle, flags: Int = 0) {
            context.startActivity(Intent(context, SecondActivity::class.java)
                    .putExtra(EXTRA_CONTROLLER_CLASS, controllerClass)
                    .putExtra(EXTRA_CONTROLLER_PARAMS, params)
                    .setFlags(flags))
        }

        fun start(context: Context, controller: Controller, flags: Int = 0) {
            start(context, controller::class.java, controller.args, flags)
        }
    }
}