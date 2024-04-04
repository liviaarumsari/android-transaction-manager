package com.example.abe.ui.scanner

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.abe.R
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.fragment.app.viewModels
import com.example.abe.ABEApplication
import com.example.abe.data.network.Retrofit
import com.example.abe.data.network.ItemsRoot
import com.example.abe.data.network.TransactionItem
import com.example.abe.data.network.UploadResultCallback
import com.example.abe.databinding.FragmentScanBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

    private lateinit var user: String

    private val viewModel: ScannerViewModel by viewModels {
        ScannerViewModelFactory((activity?.application as ABEApplication).repository)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                startCamera()
            }
        }

    private val openGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            val imageFile = File(imageUri?.path.toString())
            attemptUpload(imageFile)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        cameraView = binding.camera
        cameraExecutor = Executors.newSingleThreadExecutor()

        requestPermissions()

        val sharedPref = activity?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        user = sharedPref?.getString("user", "").toString()

        binding.captureButton.setOnClickListener {
            takePicture()
        }

        binding.galleryPreviewButton.setOnClickListener {
            openGallery()
        }

        return binding.root
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val TAG = "ScannerFragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (allPermissionsGranted()) {
            startCamera()
            getLastLocation()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
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

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun attemptUpload(imageFile: File) {
        val retrofit = Retrofit()
        val context = requireContext()
        Log.d("ABE-PHO", "size: ${imageFile.length()/1024}")
        retrofit.upload(context, imageFile, this)
    }

    private fun showPreviewDialog(imageUri: Uri) {
        val dialog = Dialog(requireContext()).apply {
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
            val filePath = imageUri.path
            if (filePath != null) {
                val imageFile = File(filePath)
                attemptUpload(imageFile)

                val msg = "Photo sent!"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                Log.d(TAG, msg)

                dialog.dismiss()
            } else {
                Log.e(TAG, "File path is null for imageUri: $imageUri")
                dialog.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun takePicture() {
        val imageCapture = imageCapture

        val photoFile = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Proceed with further operations
                    val savedUri = Uri.fromFile(photoFile)
                    showPreviewDialog(savedUri)
                }
            }
        )
    }

    private fun getLastLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: android.location.Location? ->
                    location?.let {
                        latitude = location.latitude
                        longitude = location.longitude
                        Log.d(TAG, "Latitude: $latitude, Longitude: $longitude")
                        // Now you have latitude and longitude, pass them to insertTransaction
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to get location: ${e.message}", e)
                }
        } else {
            // Handle the case where location permission is not granted
            Log.e(TAG, "Location permission not granted")
            // You may want to request the permission again or handle it in some other way
        }
    }

    override fun onSuccess(uploadResponse: ItemsRoot) {
        Log.e("ABE-PHO", "Upload success")

        uploadResponse.items.items.forEach {item ->
            viewModel.insertTransaction(user, item, latitude, longitude)
        }
    }

    override fun onFailure(errorMessage: String) {
        Log.e("ABE-PHO", errorMessage)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        openGalleryLauncher.launch(intent)
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}
