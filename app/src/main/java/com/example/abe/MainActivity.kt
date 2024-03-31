package com.example.abe

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.abe.databinding.ActivityMainBinding
import com.example.abe.services.AuthService
import com.example.abe.types.FragmentListener
import com.example.abe.ui.login.LoginActivity
import com.example.abe.ui.transactions.ExportAlertDialogFragment
import com.example.abe.ui.transactions.ExportAlertDialogTypeEnum
import com.example.abe.ui.transactions.ExportLoadDialogFragment
import com.example.abe.ui.transactions.TransactionFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), ExportAlertDialogFragment.ExportAlertDialogListener,
    FragmentListener, TransactionFragment.ItemClickListener {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory((application as ABEApplication).repository)
    }

    private val filter = IntentFilter().apply {
        addAction("RANDOMIZE_TRANSACTION")
        addAction("EXPIRED_TOKEN")
    }
    private val br = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "RANDOMIZE_TRANSACTION" -> {
                    val randomAmount = intent.getIntExtra("random_amount", 0)
                    val bundle = Bundle().apply {
                        putInt("random_amount", randomAmount)
                    }
                    navController.navigate(
                        R.id.action_navigation_transactions_to_navigation_form_transaction,
                        bundle
                    )
                }

                "EXPIRED_TOKEN" -> {
                    val loginIntent = Intent(context, LoginActivity::class.java)
                    startActivity(loginIntent)
                    this@MainActivity.finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_transactions
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        LocalBroadcastManager.getInstance(this).registerReceiver(br, filter)

        val serviceIntent = Intent(this, AuthService::class.java)
        startService(serviceIntent);
    }

    override fun onIntentReceived(action: String, info: String?) {
        when (action) {
            "OPEN_FORM" -> {
                navController.navigate(R.id.action_navigation_transactions_to_navigation_form_transaction)
            }
        }
    }

    override fun onItemClicked(id: Int) {
        val bundle = Bundle()
        bundle.putBoolean("is-update", true)
        bundle.putInt("idx-id", id)
        navController.navigate(
            R.id.action_navigation_transactions_to_navigation_form_transaction,
            bundle
        )
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
        val serviceIntent = Intent(this, AuthService::class.java)
        stopService(serviceIntent);
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp() || navController.navigateUp(appBarConfiguration)
    }

}
