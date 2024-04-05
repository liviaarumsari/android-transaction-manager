package com.example.abe.ui.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.abe.ABEApplication
import com.example.abe.MainActivity
import com.example.abe.R
import com.example.abe.data.local.PreferenceDataStoreConstants
import com.example.abe.api.ItemsRoot
import com.example.abe.api.Retrofit
import com.example.abe.api.UploadResultCallback
import com.example.abe.databinding.FragmentScanBinding
import com.example.abe.utils.isConnected
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerFragment : Fragment(), UploadResultCallback {
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude = 0.0
    private var longitude = 0.0

    private val DEFAULT_LATITUDE = -6.892382
    private val DEFAULT_LONGINTUDE = 107.608352

    private lateinit var user: String

    private var isProcessingPhoto = false

    private lateinit var waitingUploadDialog: Dialog

    private val viewModel: ScannerViewModel by viewModels {
        ScannerViewModelFactory((activity?.application as ABEApplication).repository)
    }

    private var isRequestingPermission = false

    private var uploadResponse: ItemsRoot? = null

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            isRequestingPermission = false
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please allow access to camera to use scanner",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun uriToFile(imageUri: Uri): File {
        val context = requireContext()
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir).apply {
            outputStream().use { fileOut ->
                inputStream?.copyTo(fileOut)
            }
        }
        inputStream?.close()
        return tempFile
    }

    private val openGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    val imageFile = uriToFile(imageUri)
                    attemptUpload(imageFile)

                    val msg = "Uploading photo, please wait"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                } else {
                    val msg = "Failed to fetch image from gallery"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    isProcessingPhoto = false
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        cameraView = binding.camera
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (!cameraPermissionGranted()) {
            requestCameraPermission()
        } else {
            startCamera()
        }

        lifecycleScope.launch {
            user = (activity as MainActivity).preferenceDataStoreHelper.getFirstPreference(
                PreferenceDataStoreConstants.USER, ""
            )
        }

        binding.captureButton.setOnClickListener {
            if (cameraPermissionGranted()) {
                takePicture()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please allow camera to take photos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.galleryPreviewButton.setOnClickListener {
            openGallery()
        }

        waitingUploadDialog = Dialog(requireContext()).apply {
            setCancelable(false)
            setContentView(R.layout.dialog_waiting_upload)
        }

        return binding.root
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(cameraView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                exc.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun attemptUpload(imageFile: File) {
        waitingUploadDialog.show()

        lifecycleScope.launch {
            val retrofit = Retrofit()
            val token = (activity as MainActivity).preferenceDataStoreHelper.getFirstPreference(
                PreferenceDataStoreConstants.TOKEN,
                ""
            )
            retrofit.upload(token, imageFile, this@ScannerFragment)
        }
    }

    private fun showPreviewDialog(imageUri: Uri) {
        val dialog = Dialog(requireContext()).apply {
            setCancelable(false)
            setContentView(R.layout.dialog_image_preview)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val imageView = dialog.findViewById<ImageView>(R.id.dialog_image_view)

        Glide.with(requireContext())
            .load(imageUri)
            .into(imageView)

        val confirmButton = dialog.findViewById<Button>(R.id.confirm_button)
        val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)

        confirmButton.setOnClickListener {
            val activity = activity as MainActivity
            if (!isConnected(activity.getNetworkState())) {
                dialog.dismiss()
                binding.scanLayout.visibility = View.GONE
                binding.noNetworkLayout.visibility = View.VISIBLE
                isProcessingPhoto = false
            } else {
                val filePath = imageUri.path
                if (filePath != null) {
                    val imageFile = File(filePath)
                    attemptUpload(imageFile)

                    val msg = "Uploading photo, please wait"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()

                    dialog.dismiss()
                } else {
                    isProcessingPhoto = false
                    dialog.dismiss()
                }
            }
        }

        binding.btnTryAgain.setOnClickListener {
            binding.noNetworkLayout.visibility = View.GONE
            binding.scanLayout.visibility = View.VISIBLE
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
            isProcessingPhoto = false
        }

        dialog.show()
    }

    private fun takePicture() {
        if (isProcessingPhoto) {
            Toast.makeText(
                requireContext(),
                "Unable to take picture, processing previous image",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val imageCapture = imageCapture

        val photoFile = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        isProcessingPhoto = true
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireContext(), "Photo capture failed", Toast.LENGTH_SHORT)
                        .show()
                    isProcessingPhoto = false
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    showPreviewDialog(savedUri)
                }
            }
        )
    }

    private fun setLocationAsDefault() {
        latitude = DEFAULT_LATITUDE
        longitude = DEFAULT_LONGINTUDE
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationAndInsertTrx() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (checkLocationPermissions() && checkIfLocationEnabled()) {
            fusedLocationClient.lastLocation
                .addOnCompleteListener(requireActivity()) { task ->
                    val location = task.result
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                    } else {
                        setLocationAsDefault()
                    }
                    insertItems()
                }
        } else {
            setLocationAsDefault()
            insertItems()
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkIfLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun askForLocationPermissions() {
        requestLocationLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    private val requestLocationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var granted = false
            permissions.entries.forEach {
                if (it.value) granted = true
            }
            if (granted) {
                getCurrentLocationAndInsertTrx()
            } else {
                setLocationAsDefault()
                insertItems()
            }
        }

    override fun onSuccess(uploadResponse: ItemsRoot) {
        this.uploadResponse = uploadResponse

        if (checkLocationPermissions()) {
            getCurrentLocationAndInsertTrx()
        } else {
            askForLocationPermissions()
        }
    }

    fun insertItems() {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val locationList: MutableList<Address> =
            geocoder.getFromLocation(latitude, longitude, 1) ?: mutableListOf<Address>()
        val location =
            if (locationList.size > 0) (locationList[0].getAddressLine(0)) else "Unknown location"

        uploadResponse?.items?.items?.forEach { item ->
            viewModel.insertTransaction(user, item, latitude, longitude, location)
        }

        waitingUploadDialog.dismiss()
        val msg = "New transactions added!"
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

        findNavController().navigate(R.id.action_navigation_scanner_to_navigation_transactions)
        uploadResponse = null
        isProcessingPhoto = false
    }

    override fun onFailure(errorMessage: String) {
        Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
        Log.e("ABE-PHO", errorMessage)
        isProcessingPhoto = false
        waitingUploadDialog.dismiss()
    }

    private fun openGallery() {
        if (isProcessingPhoto) {
            Toast.makeText(
                requireContext(),
                "Unable choose image, processing previous image",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        isProcessingPhoto = true
        openGalleryLauncher.launch(intent)
    }

    private fun cameraPermissionGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermission() {
        isRequestingPermission = true
        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}
