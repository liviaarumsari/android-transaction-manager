package com.example.abe.ui.form_transaction

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.abe.ABEApplication
import com.example.abe.R
import com.example.abe.databinding.FragmentFormTransactionBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class FormTransaction : Fragment() {
    private var _binding: FragmentFormTransactionBinding? = null

    private val binding get() = _binding!!


    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionId = 5

    private val viewModel: FormTransactionViewModel by viewModels {
        FormTransactionViewModelFactory((activity?.application as ABEApplication).repository)
    }

    private lateinit var user: String

    private var id: Int? = null
    private var location: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        _binding = FragmentFormTransactionBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.amountNumber.observe(viewLifecycleOwner, {})

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

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
        openInMapListener()
        locationFocusListener()

        saveButtonListener()
        deleteButtonListener()

        getLocation()

        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        user = sharedPref.getString("user", "").toString()

        if (arguments != null) {
            val args = Bundle(arguments)
            if (args.containsKey("idx-id")) {
                val trxId = args.getInt("idx-id")
                displayTrx(trxId)
            } else if (args.containsKey("random_amount")) {
                viewModel.setRandomAmount(args.getInt("random_amount"))
                useNewTrxLayout()
            }
            arguments = null
        } else {
            useNewTrxLayout()
        }

        return binding.root
    }

    private fun useNewTrxLayout() {
        binding.btnDelete.visibility = View.GONE
        binding.btnOpenMap.visibility = View.GONE

        ConstraintSet().apply {
            clone(binding.formTransaction)
            connect(
                R.id.formLocationContainer,
                ConstraintSet.TOP,
                R.id.formCategoryContainer,
                ConstraintSet.BOTTOM,
                5
            )
            applyTo(binding.formTransaction)
        }
    }

    private fun displayTrx(idTrx: Int) {
        id = idTrx
        viewModel.getTransaction(idTrx)
        binding.categoryAutocomplete.isEnabled = false
    }

    private fun titleFocusListener() {
        binding.formTitleEditText.setOnFocusChangeListener { _, focused ->
            if (!focused) setHelperText(binding.formTitleContainer, binding.formTitleEditText)
        }
    }

    private fun amountFocusListener() {
        binding.formAmountEditText.setOnFocusChangeListener { _, focused ->
            if (!focused) setHelperText(binding.formAmountContainer, binding.formAmountEditText)
        }
    }

    private fun categoryFocusListener() {
        binding.categoryAutocomplete.setOnFocusChangeListener { _, focused ->
            if (!focused) setHelperText(binding.formCategoryContainer, binding.categoryAutocomplete)
        }
    }

    private fun openInMapListener() {
        binding.btnOpenMap.setOnClickListener {
            val destinationLatitude = viewModel.latitude.value.toString()
            val destinationLongitude = viewModel.longitude.value.toString()

            val mapUri =
                Uri.parse("https://maps.google.com/maps?daddr=$destinationLatitude,$destinationLongitude")
            val intent = Intent(Intent.ACTION_VIEW, mapUri)
            startActivity(intent)
        }
    }

    private fun locationFocusListener() {
        binding.formLocationEditText.setOnFocusChangeListener { _, focused ->
            if (!focused) setHelperText(binding.formLocationContainer, binding.formLocationEditText)
        }
    }

    private fun setHelperText(container: TextInputLayout, editText: EditText) {
        container.helperText =
            if (editText.text.toString().isEmpty()) "This field is required" else null
    }

    private fun saveButtonListener() {
        binding.btnSave.setOnClickListener {
            setHelperText(binding.formTitleContainer, binding.formTitleEditText)
            setHelperText(binding.formAmountContainer, binding.formAmountEditText)
            setHelperText(binding.formCategoryContainer, binding.categoryAutocomplete)

            if (binding.formLocationEditText.text.toString().isEmpty()) {
                binding.formLocationEditText.setText(location)
                Log.v("abecekut", "location is $location")
            }

            if (binding.formTitleEditText.text.toString()
                    .isNotEmpty() && binding.formAmountEditText.text.toString()
                    .isNotEmpty() && binding.categoryAutocomplete.text.toString().isNotEmpty()
            ) {
                if (id != null) {
                    viewModel.updateTransaction(id!!)
                } else {
                    viewModel.insertTransaction(user)
                }
                findNavController().navigateUp()
            }
        }
    }

    private fun deleteButtonListener() {
        binding.btnDelete.setOnClickListener {
            id?.let {
                val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                builder.setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete") { dialog, which ->
                        viewModel.deleteTransaction(id!!)
                        findNavController().navigateUp()
                    }.setNegativeButton("Cancel") { _, _ -> }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

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
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun askForPermissions() {
        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    private var requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var granted = false
        permissions.entries.forEach {
            if (it.value) granted = true
        }
        if (granted) {
            getCurrentLocation()
        } else {
            val defaultLatitude = -6.892382
            val defaultLongitude = 107.608352
            Toast.makeText(requireActivity(), "Location set to default", Toast.LENGTH_SHORT).show()
            setLocation(defaultLatitude, defaultLongitude)
        }
    }

    private fun getLocation() {
        if (checkPermissions()) {
            getCurrentLocation()
        } else {
            askForPermissions()
        }
    }

    private fun setLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val list: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
        viewModel.latitude.value = latitude
        viewModel.longitude.value = longitude
        if (list != null) {
            // TODO: check why this sometimes doesn't update the ui
            viewModel.location.value = (list[0].getAddressLine(0))
            location = (list[0].getAddressLine(0))
//            Toast.makeText(requireActivity(), viewModel.location.value, Toast.LENGTH_SHORT)
//                .show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val defaultLatitude = -6.892382
        val defaultLongitude = 107.608352

        if (checkPermissions() && isLocationEnabled()) {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                val location: Location? = task.result
                if (location != null) {
                    setLocation(location.latitude, location.longitude)
                } else {
                    Toast.makeText(requireActivity(), "Location set to default", Toast.LENGTH_SHORT)
                        .show()
                    setLocation(defaultLatitude, defaultLongitude)
                }
            }
        } else {
            Toast.makeText(requireActivity(), "Location set to default", Toast.LENGTH_SHORT).show()
            setLocation(defaultLatitude, defaultLongitude)
        }
    }

}