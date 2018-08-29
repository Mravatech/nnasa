package com.mnassa.screen.login.enterphone

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.mnassa.R
import kotlinx.android.synthetic.main.item_country_code_drop_down.view.*
import kotlinx.android.synthetic.main.item_county_code.view.*

/**
 * Created by Peter on 3/1/2018.
 */
class CountryCodeAdapter(context: Context, data: MutableList<CountryCode>) : ArrayAdapter<CountryCode>(context, 0, 0, data) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
                ?: LayoutInflater.from(parent.context).inflate(R.layout.item_county_code, parent, false)
        val item = getItem(position)
        with(view) {
            ivCountryFlagSelected.setImageResource(item.flagRes)
            tvCountryPhoneCodeSelected.text = item.phonePrefix.visibleCode
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
                ?: LayoutInflater.from(parent.context).inflate(R.layout.item_country_code_drop_down, parent, false)
        val item = getItem(position)
        with(view) {
            ivCountryFlagDropDown.setImageResource(item.flagRes)
            val countryName by item.name
            tvCountryNameDropDown.text = countryName
            tvCountryCodeDropDown.text = "(${item.phonePrefix.visibleCode})"
        }
        return view
    }
}