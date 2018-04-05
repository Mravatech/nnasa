package com.mnassa.widget.input

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.mnassa.R
import com.mnassa.helper.DialogHelper
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.company_status_fake_edit_text.view.*
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/16/2018
 */

class StatusSelectableEditText : LinearLayout {

    private val kodeine by closestKodein()
    private val dialogHelper: DialogHelper by kodeine.instance()
    private var occupationPositionInList = -1

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val statuses = listOf(fromDictionary(R.string.reg_company_status_commercial),
            fromDictionary(R.string.reg_company_status_governmental),
            fromDictionary(R.string.reg_company_status_non_profit)
    )

    init {
        inflate(context, R.layout.company_status_fake_edit_text, this)
        tvCompanyStatusView.text = fromDictionary(R.string.reg_company_status)
        tvCompanyStatusLabel.hint = fromDictionary(R.string.reg_company_status_label)

        tvCompanyStatusView.setOnClickListener {
            dialogHelper.showChooseCompanyStatusDialog(context, statuses, occupationPositionInList, {
                tvCompanyStatusLabel.visibility = View.VISIBLE
                tvCompanyStatusView.text = statuses[it]
                occupationPositionInList = it
            })
        }
    }

    fun getOrganizationType(): String? {
        return tvCompanyStatusView.text.toString().takeIf { it != fromDictionary(R.string.reg_company_status) }
    }

    fun setOrganization(text: String?) {
        text?.let {
            tvCompanyStatusLabel.visibility = View.VISIBLE
            tvCompanyStatusView.text = it
            for ((index, status) in statuses.withIndex()) {
                if (status == text) {
                    occupationPositionInList = index
                }
            }
        }
    }

}