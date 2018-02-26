package com.mnassa.data.repository.dictionary

import android.content.Context
import com.mnassa.domain.models.TranslatedWord
import com.mnassa.domain.models.impl.TranslatedWordImpl
import com.mnassa.domain.other.AppInfoProvider
import timber.log.Timber

/**
 * Created by Peter on 23.02.2018.
 */
internal class DictionaryResources(private val context: Context, private val appInfoProvider: AppInfoProvider) {
    fun getWord(id: String): TranslatedWord? {
        val infoId = context.resources.getIdentifier("${id}_INFO", "string", context.packageName)
        if (infoId == 0) return null
        val engId = context.resources.getIdentifier("${id}_ENG", "string", context.packageName)
        val arId = context.resources.getIdentifier("${id}_AR", "string", context.packageName)

        val infoVal = context.getString(infoId)
        var engVal = if (engId != 0) context.getString(engId) else null
        var arVal = if (arId != 0) context.getString(arId) else null

        if (engVal.isNullOrBlank() || engVal == "null") {
            engVal = null
        }
        if (arVal.isNullOrBlank() || arVal == "null") {
            arVal = null
        }
        return TranslatedWordImpl(id, infoVal, engVal, arVal)
    }

    fun print(words: List<TranslatedWord>) {
        if (appInfoProvider.isDebug) {
            words.forEach {
                with(it) {
                    Timber.i("<string name=\"${id}_ENG\">$engTranslate</string>")
                    Timber.i("<string name=\"${id}_AR\">$arabicTranslate</string>")
                    Timber.i("<string name=\"${id}_INFO\">$info</string>")
                }
            }
        }
    }
}