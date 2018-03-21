package com.mnassa.screen.invite.history

import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.PhoneContactInvited
import java.text.DateFormatSymbols
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/20/2018
 */

class InviteHistoryAdapter(private val data: MutableList<PhoneContactInvited>) : RecyclerView.Adapter<InviteHistoryHolder>() {

    private var filtered: List<PhoneContactInvited>
    private var dates = EMPTY_STRING
    private var positions = SparseArray<String>()

    init {
        filtered = data.sortedWith(compareBy(PhoneContactInvited::createdAt))
        handleSections()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            InviteHistoryHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_invite_history, parent, false))

    override fun getItemCount() = filtered.size

    override fun onBindViewHolder(holder: InviteHistoryHolder, position: Int) {
        val date: String? = positions[position]
        holder.setup(filtered[position], date)
    }

    fun search(newText: String) {
        filtered = data.filter { it.description?.toLowerCase()?.startsWith(newText.toLowerCase())?: newText.isEmpty() }
        handleSections()
        notifyDataSetChanged()
    }

    private fun handleSections() {
        dates = EMPTY_STRING
        positions.clear()
        val cal: Calendar = Calendar.getInstance()
        for ((index, contact) in filtered.withIndex()) {
            cal.timeInMillis = contact.createdAt
            val stringDate = "${cal.get(Calendar.MONTH)} ${cal.get(Calendar.YEAR)}"
            if (dates != stringDate) {
                dates = stringDate
                positions.put(index, "${DateFormatSymbols(Locale.ENGLISH).months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}")
            }
        }
    }

    companion object {
        const val EMPTY_STRING = ""
    }

}