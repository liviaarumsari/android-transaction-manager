package com.example.abe

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.abe.databinding.ActivityMainBinding
import com.example.abe.types.FragmentListener
import com.example.abe.ui.transactions.ExportAlertDialogFragment
import com.example.abe.ui.transactions.ExportAlertDialogTypeEnum
import com.example.abe.ui.transactions.ExportLoadDialogFragment
import com.example.abe.ui.transactions.TransactionFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), ExportAlertDialogFragment.ExportAlertDialogListener, FragmentListener, TransactionFragment.ItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory((application as ABEApplication).repository)
    }

    private val filter = IntentFilter().apply { addAction("RANDOMIZE_TRANSACTION") }
    private val br: BroadcastReceiver = TransactionBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_transactions
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        LocalBroadcastManager.getInstance(this).registerReceiver(br, filter)
    }

    override fun onIntentReceived(action: String, info: String?) {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        when(action) {
            "OPEN_FORM" -> {
                navController.navigate(R.id.navigation_form_transaction)
            }
        }
    }

    override fun onItemClicked(id: Int) {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            Log.d("ABE-ADD", "Destination changed listener")
//            if (destination.id == R.id.navigation_form_transaction) {
//                Log.d("ABE-ADD", "Destination destination correct")
//                val fragment = supportFragmentManager.findFragmentById(R.id.navigation_form_transaction) as? FormTransaction
//                Log.d("ABE-ADD", if (fragment == null) "fragment is null" else "fragment not null")
//                fragment?.displayTrx(id)
//            }
//        }
        val bundle = Bundle()
        bundle.putBoolean("is-update", true)
        bundle.putInt("idx-id", id)
        navController.navigate(R.id.navigation_form_transaction, bundle)
    }
    override fun onNewExcelFormatClick(dialog: DialogFragment, type: ExportAlertDialogTypeEnum) {
        viewModel.newExcelFormat = true
        when (type) {
            ExportAlertDialogTypeEnum.EXPORT -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    putExtra(Intent.EXTRA_TITLE, viewModel.getExportFileName())
                }
                resultLauncher.launch(intent)
            }

            ExportAlertDialogTypeEnum.SEND_EMAIL -> {
                sendEmail()
            }
        }
    }

    override fun onOldExcelFormatClick(dialog: DialogFragment, type: ExportAlertDialogTypeEnum) {
        viewModel.newExcelFormat = false
        when (type) {
            ExportAlertDialogTypeEnum.EXPORT -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    setType("application/vnd.ms-excel")
                    putExtra(Intent.EXTRA_TITLE, viewModel.getExportFileName())
                }
                resultLauncher.launch(intent)
            }

            ExportAlertDialogTypeEnum.SEND_EMAIL -> {
                sendEmail()
            }
        }
    }

    private fun sendEmail() {
        lifecycleScope.launch {
            val exportLoadDialog = ExportLoadDialogFragment()

            exportLoadDialog.show(supportFragmentManager, "LOAD_DIALOG")
            val intent = viewModel.createEmailIntent(applicationContext)
            exportLoadDialog.dismiss()

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(intent, "Choose email app.."))
            }
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.also { uri ->
                    lifecycleScope.launch {
                        viewModel.exportTransactionsToExcel(
                            applicationContext.contentResolver, uri
                        )
                    }
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).registerReceiver(br, filter)
    }
}
