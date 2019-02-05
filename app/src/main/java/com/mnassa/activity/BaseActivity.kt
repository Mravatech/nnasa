package com.mnassa.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.translation.LanguageProviderImpl
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.KodeinContext
import org.kodein.di.android.closestKodein
import org.kodein.di.android.retainedKodein
import org.kodein.di.generic.instance
import org.kodein.di.generic.kcontext
import java.util.*

/**
 * @author Artem Chepurnoy
 */
abstract class BaseActivity : AppCompatActivity(), KodeinAware {

    @Suppress("LeakingThis")
    override val kodeinContext: KodeinContext<*> = kcontext(this)

    private val _parentKodein by closestKodein()

    override val kodein: Kodein by retainedKodein {
        extend(_parentKodein, allowOverride = true)
    }

    private val languageProvider: LanguageProvider by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        setupLanguageProvider()
        super.onCreate(savedInstanceState)
    }

    private fun setupLanguageProvider() {
        val prefs =
            getSharedPreferences(LanguageProviderImpl.LANGUAGE_PREFERENCE, Context.MODE_PRIVATE)
        val lang = prefs.getString(LanguageProviderImpl.LANGUAGE_SETTINGS, null)
        lang?.let {
            languageProvider.locale = Locale(it)
        }
    }

}