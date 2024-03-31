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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.abe.ABEApplication
import com.example.abe.R
import com.example.abe.databinding.FragmentFormTransactionBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class FormTransaction : Fragment() {
    private var _binding: FragmentFormTransactionBinding? = null

    private val binding get() = _binding!!


    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionId = 2

    private val viewModel: FormTransactionViewModel by viewModels {
        FormTransactionViewModelFactory((activity?.application as ABEApplication).repository)
    }

    private lateinit var user:String

    private var id: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        _binding = FragmentFormTransactionBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.amountNumber.observe(viewLifecycleOwner, {})

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (requireActivity().intent.hasExtra("random_amount")) {
            viewModel.setRandomAmount(requireActivity().intent.getIntExtra("random_amount", 10000))
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }


        val categories = resources.getStringArray(R.array.Categories)
        val adapterItems = ArrayAdapter<String>(requireContext(), R.layout.list_item, categories)
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

        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        user = sharedPref.getString("user", "").toString()

        if (arguments != null) {
            val args = Bundle(arguments)
            val trxId = args.getInt("idx-id")
            displayTrx(trxId)
        } else {
            binding.btnDelete.visibility = View.GONE
        }

        return binding.root
    }


    private fun displayTrx(idTrx: Int) {
        id = idTrx
        viewModel.getTransaction(idTrx)
        binding.categoryAutocomplete.isEnabled = false
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
                if (id != null) {
                    viewModel.updateTransaction(id!!)
                }
                else {
                    viewModel.insertTransaction(user)
                }
                findNavController().navigateUp()
            }
        }
    }

    private fun deleteButtonListener() {
        binding.btnDelete.setOnClickListener {
            id?.let { viewModel.deleteTransaction(id!!)}
            findNavController().navigateUp()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
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
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
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
                        Log.v("ABECEKUT", "location not available")
                    }
                }
            } else {
                Toast.makeText(requireActivity(), "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }
}