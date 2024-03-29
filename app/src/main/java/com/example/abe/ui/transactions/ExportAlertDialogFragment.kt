package com.example.abe.ui.transactions

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ExportAlertDialogFragment : DialogFragment() {
    private lateinit var listener: ExportAlertDialogListener
    private lateinit var type: ExportAlertDialogTypeEnum

    companion object {
        private const val TYPE = "type"

        fun newInstance(
            type: ExportAlertDialogTypeEnum
        ): ExportAlertDialogFragment = ExportAlertDialogFragment().apply {
            arguments = Bundle().apply {
                putString(TYPE, type.toString())
            }
        }
    }

    interface ExportAlertDialogListener {
        fun onNewExcelFormatClick(dialog: DialogFragment, type: ExportAlertDialogTypeEnum)
        fun onOldExcelFormatClick(dialog: DialogFragment, type: ExportAlertDialogTypeEnum)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = ExportAlertDialogTypeEnum.valueOf(arguments?.getString(TYPE) ?: "EXPORT")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as ExportAlertDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                (context.toString() +
                        " must implement NoticeDialogListener")
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Choose Excel File Format")
                .setPositiveButton("Xlxs") { dialog, id ->
                    listener.onNewExcelFormatClick(this, type)
                }
                .setNegativeButton("Xls") { dialog, id ->
                    listener.onOldExcelFormatClick(this, type)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}