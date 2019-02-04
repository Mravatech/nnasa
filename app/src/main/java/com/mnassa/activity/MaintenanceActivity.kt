package com.mnassa.activity

import android.content.Intent
import com.mnassa.screen.maintenance.MaintenanceController

/**
 * @author Artem Chepurnoy
 */
class MaintenanceActivity : MainActivity() {

    override fun createRootControllerInstance() = MaintenanceController.newInstance()

    override fun processMaintenanceStatusChange(isMaintenance: Boolean) {
        // Switch back to the main activity, when the
        // maintenance mode ends.
        if (!isMaintenance) {
            switchToMainActivity()
        }
    }

    private fun switchToMainActivity() {
        Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .let(::startActivity)
        overridePendingTransition(0, 0)
    }

}