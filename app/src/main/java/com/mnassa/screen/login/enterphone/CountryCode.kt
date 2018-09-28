package com.mnassa.screen.login.enterphone

import android.text.InputFilter
import com.mnassa.R
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.translation.fromDictionary
import java.io.Serializable

/**
 * Created by Peter on 3/1/2018.
 */
data class CountryCode(val flagRes: Int, val name: TranslatedWordModel, val phonePrefix: PhonePrefix) : Serializable

fun CountryCode.withTail(tail: String): PhoneNumber = PhoneNumber(this, tail)
private val arabicDigits = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
private val europDigits = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

sealed class PhonePrefix : Serializable {
    abstract val normalizedCode: String
    abstract val visibleCode: String

    object SaudiArabia : PhonePrefix() { //+966  +٩٦٦
        override val normalizedCode: String = "966"
        override val visibleCode: String get() = fromDictionary(R.string.country_code_arabia)
    }

    object Ukraine : PhonePrefix() { //+380   +٣٨٠
        override val normalizedCode: String = "380"
        override val visibleCode: String get() = fromDictionary(R.string.country_code_ukraine)
    }

    object UnitedState : PhonePrefix() { //+1 +١
        override val normalizedCode: String = "1"
        override val visibleCode: String get() = fromDictionary(R.string.country_code_usa)
    }

    object Canada : PhonePrefix() { //+1 +١
        override val normalizedCode: String = "1"
        override val visibleCode: String get() = fromDictionary(R.string.country_code_canada)
    }
}

class PhoneNumber(
        private val countryCode: CountryCode?,
        private val tail: String
) {
    val isValid: Boolean
        get() {
            if (countryCode == null) return false
            val normalizedPhone = normalize() ?: return false
            return isValid(normalizedPhone)
        }

    /**
     * Returns normalized phone number without "+"
     */
    fun normalize(): String? {
        val countryCode = countryCode?.phonePrefix?.normalizedCode ?: return null
        val tail = tail
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "")
        return countryCode + tail.map {
            arabicDigits.indexOf(it).takeIf { it >= 0 }
                    ?: europDigits.indexOf(it).takeIf { it >= 0 } ?: return null
        }.joinToString(separator = "")
    }

    companion object {
        fun isValid(normalizedPhone: String?): Boolean {
            normalizedPhone ?: return false
            when {
                normalizedPhone.startsWith(PhonePrefix.SaudiArabia.normalizedCode) ||
                        normalizedPhone.startsWith(PhonePrefix.Ukraine.normalizedCode) -> {
                    //+966 565-585-868
                    return normalizedPhone.length == 12
                }
                normalizedPhone.startsWith(PhonePrefix.UnitedState.normalizedCode) ||
                        normalizedPhone.startsWith(PhonePrefix.Canada.normalizedCode) -> {
                    //+1 888-452-1505
                    return normalizedPhone.length == 11
                }
            }
            return false
        }
    }
}

val PHONE_INPUT_FILTER = InputFilter { source, start, end, dest, dstart, dend ->
    val result = StringBuilder(source.length)
    for (i in start until end) {
        if (Character.isDigit(source[i])) result.append(source[i])
    }
    result
}

