package com.mnassa

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.mnassa.domain.other.AppInfoProvider

/**
 * Created by Peter on 2/21/2018.
 */
class AppInfoProviderImpl(private val context: Context) : AppInfoProvider {
    @SuppressLint("HardwareIds")
    private val androidIdVal = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    private val manufacturer = Build.MANUFACTURER
    private val model = Build.MODEL
    private val deviceNameVal = if (model.startsWith(manufacturer)) model else "$manufacturer $model"

    override val androidId: String = androidIdVal
    override val deviceName: String = deviceNameVal
    override val isDebug: Boolean = BuildConfig.DEBUG
    override val applicationId: String = BuildConfig.APPLICATION_ID
    override val buildType: String = BuildConfig.BUILD_TYPE
    override val versionCode: Int = BuildConfig.VERSION_CODE
    override val versionName: String = BuildConfig.VERSION_NAME
    override val osVersion: String = Build.VERSION.RELEASE
    override val endpoint: String = BuildConfig.ENDPOINT
    override val appName: String = context.getString(R.string.app_name)
}