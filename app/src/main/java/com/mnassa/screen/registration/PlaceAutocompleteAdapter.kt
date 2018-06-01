package com.mnassa.screen.registration

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.google.android.gms.location.places.AutocompletePrediction
import com.mnassa.domain.model.GeoPlaceModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/5/2018
 */

class PlaceAutocompleteAdapter(
    context: Context,
    private val placeAutoCompleteListener: PlaceAutoCompleteListener
)
    : ArrayAdapter<GeoPlaceModel>(context,
        android.R.layout.simple_expandable_list_item_2,
        android.R.id.text1), Filterable {

    private var resultList = ArrayList<GeoPlaceModel>()

    override fun getCount(): Int {
        return resultList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = super.getView(position, convertView, parent)
        val item = getItem(position)
        item?.let {
            val textView1 = row.findViewById<TextView>(android.R.id.text1)
            val textView2 = row.findViewById<TextView>(android.R.id.text2)
            textView1.text = item.primaryText
            textView2.text = item.secondaryText
        }
        return row
    }

    override fun getItem(position: Int): GeoPlaceModel? {
        return resultList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val c = constraint ?: return results
                val autocompletePredictions = placeAutoCompleteListener.getAutocomplete(c)
                results.values = autocompletePredictions
                results.count = autocompletePredictions.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                results?.takeIf { it.count > 0 }?.apply {
                    resultList = results.values as ArrayList<GeoPlaceModel>
                    notifyDataSetChanged()
                } ?: run {
                    notifyDataSetInvalidated()
                }
            }

            override fun convertResultToString(resultValue: Any): CharSequence {
                return when (resultValue) {
                    is AutocompletePrediction -> resultValue.getFullText(null)
                    else -> super.convertResultToString(resultValue)
                }
            }
        }
    }

    interface PlaceAutoCompleteListener {
        fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel>
    }
}