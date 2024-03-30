package com.example.abe.ui.transactions

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.abe.R
import com.example.abe.data.Transaction
import com.example.abe.domain.FormatCurrencyUseCase
import com.example.abe.ui.add_transaction.FormTransaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Locale

class TransactionsAdapter: ListAdapter<Transaction, TransactionsAdapter.TransactionViewHolder>(TransactionComparator()) {
    class TransactionViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val clImageContainer: ConstraintLayout
        val ivTrxIcon: ImageView

        val tvTrxTitle: TextView
        val tvLocation: TextView
        val tvDate: TextView
        val tvAmount: TextView

        init {
            clImageContainer = view.findViewById(R.id.clImageContainer)
            ivTrxIcon = view.findViewById(R.id.ivTrxIcon)

            tvTrxTitle = view.findViewById(R.id.tvTrxTitle)
            tvLocation = view.findViewById(R.id.tvLocation)
            tvDate = view.findViewById(R.id.tvDate)
            tvAmount = view.findViewById(R.id.tvAmount)
        }
    }

    class TransactionComparator: DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_row_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        with(holder) {
//            TODO Set color from theme
//            clImageContainer.background =
            val trx = getItem(position)

            if (!trx.isExpense)
                ivTrxIcon.setImageResource(R.drawable.ic_circle_arrow_up)

            tvTrxTitle.text = trx.title
//            TODO location
            tvLocation.text = trx.location

            tvDate.text = SimpleDateFormat("d MMM yyyy" , Locale.ENGLISH).format(trx.timestamp)

            val currencyFormatter = FormatCurrencyUseCase()
            val amountText = (if (trx.isExpense) "-" else "+") + currencyFormatter(trx.amount)
            tvAmount.text = amountText

            view.setOnClickListener {
                val intent = Intent(it.context, FormTransaction::class.java)
                intent.putExtra("id", trx.id.toString())
                it.context.startActivity(intent)
            }
        }
    }
}