package com.mnassa.core.permissions

import android.app.Activity
import android.app.Fragment
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.UiThread
import com.bluelinelabs.conductor.Controller
import com.mnassa.core.events.CompositeEventListener
import com.mnassa.core.events.awaitFirst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Peter on 19.01.18.
 */
typealias SupportFragment = androidx.fragment.app.Fragment

/**
 * Delegate for [PermissionsManager]
 *
 * @param permissionsObservable - observer for [OnRequestPermissionsResultEvent] events
 * @param view - suspend function which belongs to view.
 * View should be one of:
 * [android.app.Activity],
 * [android.app.Fragment],
 * [SupportFragment],
 * */
class PermissionsManagerDelegate(
        private val permissionsObservable: CompositeEventListener<OnRequestPermissionsResultEvent>,
        private val view: suspend () -> Any) : PermissionsManager {

    /**
     * @see [PermissionsManager.requestPermissions]
     * */
    override suspend fun requestPermissions(permissions: List<String>): RequestPermissionsResult {
        //we always use UI thread to work with permissions
        return withContext(Dispatchers.Main) { requestPermissionsImpl(permissions) }
    }

    /**
     * @see [PermissionsManager.requestPermissionsOrThrow]
     * */
    override suspend fun requestPermissionsOrThrow(permissions: List<String>): RequestPermissionsResult {
        val result = requestPermissions(permissions)
        if (!result.isAllGranted) {
            throw PermissionException("Some permissions were denied: ${result.deniedPermissions.joinToString()}", result)
        }
        return result
    }

    /**
     * @see [PermissionsManager.shouldShowRequestPermissionRationale]
     * */
    override suspend fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        //we always use UI thread to work with permissions
        return withContext(Dispatchers.Main) { shouldShowRequestPermissionRationaleImpl(permission) }
    }

    /**
     * Implementation of permissions request. Should be called on UI thread
     *
     * @param permissions - list of permissions to request
     * @return [RequestPermissionsResult]
     * */
    @UiThread
    private suspend fun requestPermissionsImpl(permissions: List<String>): RequestPermissionsResult {
        if (!isMarshmallow() || permissions.all { isPermissionGranted(it) }) {
            return RequestPermissionsResult(grantedPermissions = permissions, deniedPermissions = emptyList())
        }

        // You can request only one permissions set at once;
        // otherwise second permissions set will be denied.
        // So, to prevent it, we use static mutex here
        REQUEST_PERMISSION_MUTEX.lock()
        try {
            val requestCode = generateRequestCode()

            val v = view()
            when (v) {
                is Activity -> v.requestPermissions(permissions.toTypedArray(), requestCode)
                is Fragment -> v.requestPermissions(permissions.toTypedArray(), requestCode)
                is SupportFragment -> v.requestPermissions(permissions.toTypedArray(), requestCode)
                is Controller -> v.requestPermissions(permissions.toTypedArray(), requestCode)
                else -> throw IllegalStateException("View $v must be ${Activity::class.java.name} " +
                        "or ${Fragment::class.java.name} " +
                        "or ${SupportFragment::class.java.name} " +
                        "or ${Controller::class.java.name}!")
            }

            //wait for permissions request result
            val event = permissionsObservable.awaitFirst { it.requestCode == requestCode }
            return RequestPermissionsResult(event)
        } finally {
            REQUEST_PERMISSION_MUTEX.unlock()
        }
    }

    /**
     * Implementation of permission rational status request. Should be called on UI thread
     *
     * @param permission - permission to check
     * @return true if permission needs explanation
     * */
    @UiThread
    private suspend fun shouldShowRequestPermissionRationaleImpl(permission: String): Boolean {
        if (!isMarshmallow()) return false

        val v = view()
        return when (v) {
            is Activity -> v.shouldShowRequestPermissionRationale(permission)
            is Fragment -> v.shouldShowRequestPermissionRationale(permission)
            is SupportFragment -> v.shouldShowRequestPermissionRationale(permission)
            is Controller -> v.shouldShowRequestPermissionRationale(permission)
            else -> throw IllegalStateException("View $v must be ${Activity::class.java.name} " +
                    "or ${Fragment::class.java.name} " +
                    "or ${SupportFragment::class.java.name}!")
        }
    }

    /**
     * Implementation of permission granted status request. Should be called on UI thread
     *
     * @param permission - permission to check
     * @return true if permission is granted
     * */
    @UiThread
    private suspend fun isPermissionGranted(permission: String): Boolean {
        if (!isMarshmallow()) return true

        val v = view()
        return when (v) {
            is Activity -> v.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            is Fragment -> requireNotNull(v.activity).checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            is SupportFragment -> requireNotNull(v.context).checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            is Controller -> requireNotNull(v.applicationContext).checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            else -> throw IllegalStateException("View $v must be ${Activity::class.java.name} " +
                    "or ${Fragment::class.java.name} " +
                    "or ${SupportFragment::class.java.name}!")
        }
    }

    /**
     * Checks if permissions are available in system
     *
     * @return true if OS is Android 6 or higher
     * */
    private fun isMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    /**
     * Generates request code between 1 and 65530
     */
    private fun generateRequestCode(): Int {
        val result = REQUEST_CODE.getAndIncrement()
        if (result >= 65530) {
            REQUEST_CODE.set(1)
        }

        return result
    }

    private companion object {
        private val REQUEST_CODE = AtomicInteger(1)
        private val REQUEST_PERMISSION_MUTEX = Mutex()
    }

}