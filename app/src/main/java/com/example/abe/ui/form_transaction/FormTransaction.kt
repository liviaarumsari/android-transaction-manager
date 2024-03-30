package com.example.abe.ui.form_transaction

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.abe.ABEApplication
import com.example.abe.R
import com.example.abe.databinding.ActivityFormTransactionBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class FormTransaction : AppCompatActivity() {
    private lateinit var binding: ActivityFormTransactionBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionId = 2

    private val viewModel: FormTransactionViewModel by viewModels {
        FormTransactionViewModelFactory((this.application as ABEApplication).repository)
    }

    private lateinit var user:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFormTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.amountNumber.observe(this, {})

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (intent.hasExtra("id")) {
            intent.getStringExtra("id")?.let { viewModel.getTransaction(it.toInt()) }
            binding.categoryAutocomplete.isEnabled = false
        }
        else {
            binding.btnDelete.visibility = View.GONE
        }

        val categories = resources.getStringArray(R.array.Categories)
        val adapterItems = ArrayAdapter<String>(this, R.layout.list_item, categories)
        binding.categoryAutocomplete.setAdapter(adapterItems)
        binding.categoryAutocomplete.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val item = parent.getItemAtPosition(position).toString()
            }
        titleFocusListener()
        amountFocusListener()
        categoryFocusListener()
        locationFocusListener()

        saveButtonListener()
        deleteButtonListener()

        getLocation()

        val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        user = sharedPref.getString("user", "").toString()

    }

    private fun titleFocusListener() {
        binding.formTitleEditText.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.formTitleContainer.helperText =
                    if (binding.formTitleEditText.text.toString()
                            .isEmpty()
                    ) "Title is required" else null
            }
        }
    }

    private fun amountFocusListener() {
        binding.formAmountEditText.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.formAmountContainer.helperText =
                    if (binding.formAmountEditText.text.toString()
                            .isEmpty()
                    ) "Amount is required" else null
            }
        }
    }

    private fun categoryFocusListener() {
        binding.categoryAutocomplete.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.formCategoryContainer.helperText =
                    if (binding.categoryAutocomplete.text.toString()
                            .isEmpty()
                    ) "Category is required" else null
            }
        }
    }

    private fun locationFocusListener() {
        binding.formLocationEditText.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.formLocationContainer.helperText =
                    if (binding.formLocationEditText.text.toString()
                            .isEmpty()
                    ) "Location is required" else null
            }
        }
    }

    private fun saveButtonListener() {
        binding.btnSave.setOnClickListener {
            if (binding.formTitleEditText.text.toString()
                    .isNotEmpty() && binding.formAmountEditText.text.toString()
                    .isNotEmpty() && binding.categoryAutocomplete.text.toString()
                    .isNotEmpty() && binding.formLocationEditText.text.toString().isNotEmpty()
            ) {
                if (intent.hasExtra("id")) {
                    intent.getStringExtra("id")?.let { viewModel.updateTransaction(it.toInt()) }
                }
                else {
                    viewModel.insertTransaction(user)
                }
                finish()
            }
        }
    }

    private fun deleteButtonListener() {
        binding.btnDelete.setOnClickListener {
            intent.getStringExtra("id")?.let { viewModel.deleteTransaction(it.toInt()) }
            finish()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: MutableList<Address>? =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        Log.v(
                            "ABECEKUT",
                            location.latitude.toString() + " " + location.longitude.toString()
                        )
                        binding.viewModel?.latitude?.value = location.latitude
                        binding.viewModel?.longitude?.value = location.longitude
                        if (list != null) {
                            binding.viewModel?.location?.value = (list[0].getAddressLine(0))
                            binding.formLocationEditText.setText(list[0].getAddressLine(0))
                        }
                    } else {
                        Log.v("ABECEKUT", "location kg ada")
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }
}