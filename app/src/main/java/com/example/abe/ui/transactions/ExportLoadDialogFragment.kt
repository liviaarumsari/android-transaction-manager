package com.example.abe.ui.transactions

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.abe.R

class ExportLoadDialogFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_load_excel, null))
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}