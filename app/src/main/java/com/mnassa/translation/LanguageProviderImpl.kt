package com.mnassa.translation

import android.content.Context
import com.mnassa.domain.other.LanguageProvider
import java.util.*

/**
 * Created by Peter on 2/26/2018.
 */
class LanguageProviderImpl(private val context: Context) : LanguageProvider {
    override var locale: Locale
        get() = Locale.getDefault()
        set(value) {
            Locale.setDefault(value)
        }
    override val language: String
        get() = locale.language
}