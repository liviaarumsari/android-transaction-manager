package com.example.abe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.abe.databinding.ActivityMainBinding
import com.example.abe.ui.transactions.ExportAlertDialogFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), ExportAlertDialogFragment.ExportAlertDialogListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory((application as ABEApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_transactions))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onNewExcelFormatClick(dialog: DialogFragment) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            putExtra(Intent.EXTRA_TITLE, viewModel.getExportFileName())
            viewModel.newExcelFormat = true
        }
        resultLauncher.launch(intent)
    }

    override fun onOldExcelFormatClick(dialog: DialogFragment) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            setType("application/vnd.ms-excel")
            putExtra(Intent.EXTRA_TITLE, viewModel.getExportFileName())
            viewModel.newExcelFormat = false
        }
        resultLauncher.launch(intent)
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.also {uri ->
                viewModel.exportTransactionsToExcel(
                    applicationContext.contentResolver, uri)
            }
        }
    }
}