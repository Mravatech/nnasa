package com.mnassa.screen.wallet

import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.TransactionModel
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_wallet_transaction_income.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat

/**
 * Created by Peter on 4/2/2018.
 */
class WalletRVAdapter(languageProvider: LanguageProvider) : BaseSortedPaginationRVAdapter<TransactionModel>() {
    private val dateFormatter = SimpleDateFormat("h:mm a d MMM yyyy", languageProvider.locale)

    override val itemsComparator: (item1: TransactionModel, item2: TransactionModel) -> Int = { first, second ->
        first.time.compareTo(second.time) * -1
    }
    override val itemClass: Class<TransactionModel> = TransactionModel::class.java

    init {
        dataStorage = SortedDataStorage(itemClass, this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<TransactionModel> = when (viewType) {
        TYPE_SPENT -> TransactionViewHolder.newInstanceSpent(parent, dateFormatter)
        TYPE_GAINED -> TransactionViewHolder.newInstanceGained(parent, dateFormatter)
        else -> throw IllegalArgumentException("Illegal view type $viewType")
    }

    override fun getViewType(position: Int): Int {
        return if (dataStorage[position].amount < 0) TYPE_SPENT else TYPE_GAINED
    }

    class TransactionViewHolder(itemView: View, private val formatter: SimpleDateFormat) : BaseVH<TransactionModel>(itemView) {
        private val moneyFormatter = DecimalFormat("+#,##0;-#")

        override fun bind(item: TransactionModel) {
            with(itemView) {
                val account = when {
                    item.amount < 0 && item.toAccount != null -> requireNotNull(item.toAccount)
                    item.amount > 0 && item.fromAccount != null -> requireNotNull(item.fromAccount)
                    else -> item.toAccount ?: item.byAccount ?: item.fromAccount
                }
                tvFrom.text = account?.formattedName
                tvFrom.goneIfEmpty()
                tvType.text = item.type
                tvAmount.text = moneyFormatter.format(item.amount)
                tvTime.text = formatter.format(item.time)
                tvBalanceAfter.text = fromDictionary(R.string.wallet_balance_after).format(item.afterBalance)
                tvDescription.text = item.description
                tvDescription.goneIfEmpty()
            }
        }

        companion object {
            fun newInstanceSpent(parent: ViewGroup, formatter: SimpleDateFormat): TransactionViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallet_transaction_income, parent, false)
                with(view) {
                    tvAmount.setTextColor(ContextCompat.getColor(parent.context, R.color.money_spent))
                    tvAmount.setBackgroundResource(R.drawable.transaction_corners_spent)
                }
                return TransactionViewHolder(view, formatter)
            }

            fun newInstanceGained(parent: ViewGroup, formatter: SimpleDateFormat): TransactionViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallet_transaction_income, parent, false)
                with(view) {
                    tvAmount.setTextColor(ContextCompat.getColor(parent.context, R.color.money_gained))
                    tvAmount.setBackgroundResource(R.drawable.transaction_corners_gained)
                }
                return TransactionViewHolder(view, formatter)
            }
        }
    }

    private companion object {
        const val TYPE_SPENT = 1
        const val TYPE_GAINED = 2
    }
}