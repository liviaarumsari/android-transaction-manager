package com.example.abe.ui.transactions

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ExportAlertDialogFragment: DialogFragment() {
    internal lateinit var listener: ExportAlertDialogListener

    interface ExportAlertDialogListener {
        fun onNewExcelFormatClick(dialog: DialogFragment)
        fun onOldExcelFormatClick(dialog: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as ExportAlertDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Choose Excel File Format")
                .setPositiveButton("Xlxs") { dialog, id ->
                    listener.onNewExcelFormatClick(this)
                }
                .setNegativeButton("Xls") { dialog, id ->
                    listener.onOldExcelFormatClick(this)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}