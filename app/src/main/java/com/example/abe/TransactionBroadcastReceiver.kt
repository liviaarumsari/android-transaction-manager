package com.example.abe

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.abe.ui.form_transaction.FormTransaction


private const val TAG = "TransactionBroadcastReceiver"

class TransactionBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            "RANDOMIZE_TRANSACTION" -> {
                val newIntent = Intent(context, FormTransaction::class.java)
                newIntent.putExtra("random_amount", intent.getIntExtra("random_amount", 10000))

                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(newIntent)
            }
        }
    }
}