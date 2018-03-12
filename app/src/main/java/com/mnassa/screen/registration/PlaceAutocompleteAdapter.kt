package com.mnassa.screen.registration

import android.content.Context
import android.graphics.Typeface
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.data.DataBufferUtils
import android.widget.Toast
import java.util.concurrent.TimeUnit
import com.google.android.gms.location.places.Places
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/5/2018
 */

class PlaceAutocompleteAdapter(context: Context,
                               private var googleApiClient: GoogleApiClient,
                               private var bounds: LatLngBounds?,
                               filter: AutocompleteFilter?)
    : ArrayAdapter<AutocompletePrediction>(context,
        android.R.layout.simple_expandable_list_item_2,
        android.R.id.text1), Filterable {

    private var resultList: ArrayList<AutocompletePrediction>? = null
    private var placeFilter: AutocompleteFilter? = filter

    override fun getCount(): Int {
        return resultList?.size ?: 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = super.getView(position, convertView, parent)
        val item = getItem(position)
        item?.let {
            val textView1 = row.findViewById<TextView>(android.R.id.text1)
            val textView2 = row.findViewById<TextView>(android.R.id.text2)
            textView1.text = item.getPrimaryText(STYLE_BOLD)
            textView2.text = item.getSecondaryText(STYLE_BOLD)
        }
        return row
    }

    override fun getItem(position: Int): AutocompletePrediction? {
        return resultList?.getOrNull(position)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val c = constraint ?: return results
                val autocompletePredictions = getAutocomplete(c) ?: return results
                results.values = autocompletePredictions
                results.count = autocompletePredictions.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                results?.takeIf { it.count > 0 }?.apply {
                    resultList = results.values as ArrayList<AutocompletePrediction>?
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

    private fun getAutocomplete(constraint: CharSequence): ArrayList<AutocompletePrediction>? {
        if (googleApiClient.isConnected) {
            Timber.i("Starting autocomplete query for: $constraint")
            val results = Places.GeoDataApi.getAutocompletePredictions(
                    googleApiClient, constraint.toString(), bounds, placeFilter)
            val autocompletePredictions = results.await(60, TimeUnit.SECONDS)
            val status = autocompletePredictions.status
            if (!status.isSuccess) {
                //todo handle exception
                Toast.makeText(context, "$status",
                        Toast.LENGTH_SHORT).show()
                Timber.e("Error getting autocomplete prediction API call: $status")
                autocompletePredictions.release()
                return null
            }
            Timber.i("Query completed. Received ${autocompletePredictions.count} predictions.")
            return DataBufferUtils.freezeAndClose(autocompletePredictions)
        }
        Timber.e("Google API client is not connected for autocomplete query.")
        return null
    }

    companion object {
        private val STYLE_BOLD = StyleSpan(Typeface.BOLD)
    }
}