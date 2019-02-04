package com.mnassa.screen.termsandconditions

import android.os.Build
import androidx.annotation.RequiresApi
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.mnassa.BuildConfig
import com.mnassa.R
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_terms_and_conditions.view.*
import org.kodein.di.generic.instance


/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/10/2018
 */

class TermsAndConditionsController : MnassaControllerImpl<TermsAndConditionsViewModel>() {

    override val layoutId: Int = R.layout.controller_terms_and_conditions
    override val viewModel: TermsAndConditionsViewModel by instance()

    private val languageProvider: LanguageProvider by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.tlbrTermsAndConditions.title = fromDictionary(R.string.terms_and_conditions)
        val end = if(languageProvider.isArabian) AR else EN
        view.wvTermsAndConditions.loadUrl("${BuildConfig.TERMS_AND_CONDITIONS}$end")
        view.wvTermsAndConditions.webViewClient = object : WebViewClient() {
            @SuppressWarnings("deprecation")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return false
            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = false
        }
    }

    companion object {
        private const val AR = "terms/ar"
        private const val EN = "terms/en"
        fun newInstance() = TermsAndConditionsController()
    }
}