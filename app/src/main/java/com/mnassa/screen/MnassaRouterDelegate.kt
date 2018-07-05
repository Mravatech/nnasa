package com.mnassa.screen

import android.content.Intent
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.mnassa.activity.SecondActivity
import com.mnassa.screen.login.entercode.EnterCodeController
import com.mnassa.screen.login.enterphone.EnterPhoneController
import com.mnassa.screen.login.enterpromo.EnterPromoController
import com.mnassa.screen.login.selectaccount.SelectAccountController
import com.mnassa.screen.main.MainController
import com.mnassa.screen.registration.RegistrationController
import com.mnassa.screen.splash.SplashController
import com.mnassa.screen.termsandconditions.TermsAndConditionsController

/**
 * Created by Peter on 3/12/2018.
 */
class MnassaRouterDelegate : MnassaRouter {
    override fun open(self: Controller, controller: Controller) {

        if (openInNewActivityWithoutStack(self, controller)) {
            SecondActivity.start(
                    context = requireNotNull(self.activity),
                    controller = controller,
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            return
        }

        if (openInNewActivity(self, controller)) {
            SecondActivity.start(context = requireNotNull(self.activity), controller = controller)
            return
        }

        val router = self.router

        if (addToStack(self, controller)) {
            router.pushController(RouterTransaction.with(controller))
        } else {
            router.popToRoot()
            router.replaceTopController(RouterTransaction.with(controller))
        }
    }

    override fun close(self: Controller) {
        self.activity?.onBackPressed()
    }

    private fun addToStack(self: Controller, controller: Controller): Boolean {
        return when {
            controller is MainController -> false
            controller is EnterPromoController -> true
            self is SplashController -> false
            self is EnterPhoneController -> controller is TermsAndConditionsController
            self is EnterPromoController -> false
            self is EnterCodeController -> false
            self is RegistrationController -> false
            self is SelectAccountController -> false

            else -> true
        }
    }

    private fun openInNewActivityWithoutStack(self: Controller, controller: Controller): Boolean {
        return when (controller) {
            is MainController -> true
            else -> false
        }
    }

    private fun openInNewActivity(self: Controller, controller: Controller): Boolean {
        return isInMainController(self)
    }

    private fun isInMainController(controller: Controller): Boolean {
        return controller is MainController || controller.parentController?.let { isInMainController(it) } ?: false
    }
}