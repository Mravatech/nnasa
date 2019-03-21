package com.mnassa.domain.other


interface AppInfoProvider {
    val androidId: String
    val deviceName: String
    val isDebug: Boolean
    /**
     * `true` if app should not send requests by itself, for example
     * it should not reset counters or mark anything as opened,
     * `false` otherwise.
     */
    val isGhost: Boolean
    /**
     * `true` if app should login using custom login service,
     * `false` otherwise.
     */
    val isCustomAuth: Boolean
    val applicationId: String
    val buildType: String
    val versionCode: Int
    val versionName: String
    val osVersion: String
    val endpoint: String
    val appName: String
    val urlGoogleMapsSearch: String
}
