package com.example.abe.ui.scanner

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
import com.example.abe.data.network.Retrofit
import com.example.abe.data.network.ItemsRoot
import com.example.abe.data.network.UploadResultCallback
import com.example.abe.databinding.FragmentScanBinding
import java.io.File
import java.io.FileOutputStream
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

        binding.captureButton.setOnClickListener {
            takePicture()
        }

        binding.galleryPreviewButton.setOnClickListener {
            openGallery()
        }

        return binding.root
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
        retrofit.upload(context, imageFile, this)
    }

    private fun takePicture() {
        val imageCapture = imageCapture

        val photoFile = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, photoFile)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "ABE")
        }

        val photoUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val resolver = requireContext().contentResolver
        val parcelFileDescriptor = resolver.openFileDescriptor(photoUri!!, "w")
        val outputStream = FileOutputStream(parcelFileDescriptor!!.fileDescriptor)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputStream)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.EMPTY

                    // Create a dialog
                    val dialog = Dialog(requireContext()).apply {
                        setContentView(R.layout.dialog_image_preview)
                        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    }

                    // Get the ImageView from the dialog layout
                    val imageView = dialog.findViewById<ImageView>(R.id.dialog_image_view)

                    // Load the image into the ImageView
                    Glide.with(requireContext())
                        .load(savedUri)
                        .into(imageView)

                    // Get the confirmation button from the dialog layout
                    val confirmButton = dialog.findViewById<Button>(R.id.confirm_button)
                    val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)

                    // Set a click listener for the confirmation button
                    confirmButton.setOnClickListener {
                        val imageFile = File(savedUri.path.toString())
                        attemptUpload(imageFile)

                        val msg = "Photo sent!"
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, msg)

                        // Dismiss the dialog
                        dialog.dismiss()
                    }

                    cancelButton.setOnClickListener {
                        // Dismiss the dialog
                        dialog.dismiss()
                    }

                    // Show the dialog
                    dialog.show()
                }
            }
        )
        parcelFileDescriptor.close()
    }

    override fun onSuccess(uploadResponse: ItemsRoot) {
        // TODO send the data to database
    }

    override fun onFailure(errorMessage: String) {
        // TODO handle failed to get response
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
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }
}
