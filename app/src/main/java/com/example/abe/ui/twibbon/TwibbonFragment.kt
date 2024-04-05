package com.example.abe.ui.twibbon

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
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
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.abe.R
import com.example.abe.databinding.FragmentTwibbonBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService


class TwibbonFragment : Fragment() {
    private var _binding: FragmentTwibbonBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null

    private var isProcessingPhoto = false

    companion object {
        private const val TAG = "ABE-TWB"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTwibbonBinding.inflate(inflater, container, false)

        if (cameraPermissionsGranted()) {
            startCamera()
        } else {
            requestCameraPermissions()
        }

        binding.btnCaptureTwibbon.setOnClickListener {
            if (cameraPermissionsGranted()) {
                previewTwibbon()
            } else {
                Toast.makeText(requireContext(), "Please allow camera to take photos", Toast.LENGTH_SHORT).show()
            }
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
                    it.setSurfaceProvider(binding.prvTwibbon.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private val cameraPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        )
        { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please allow camera to use Twibbon",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun previewTwibbon() {
        if (isProcessingPhoto) {
            Toast.makeText(requireContext(), "Unable to take picture, processing previous image", Toast.LENGTH_SHORT).show()
            return
        }

        isProcessingPhoto = true
        Toast.makeText(requireContext(), "Generating twibbon", Toast.LENGTH_SHORT).show()
        val imageCapture = imageCapture ?: return
        deletePreviousTwibbons()

        val photoFile = File(
            requireContext().cacheDir,
            "twibbon_${
                SimpleDateFormat(
                    FILENAME_FORMAT,
                    Locale.US
                ).format(System.currentTimeMillis())
            }.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

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
                    overlayTwibbonToImage(savedUri)
                    showPreviewDialog(savedUri)
                }
            }
        )
    }

    private fun overlayTwibbonToImage(imageUri: Uri) {
        try {
            val exifIms = requireActivity().contentResolver.openInputStream(imageUri) ?: return
            val exif = ExifInterface(exifIms)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)

            val overlayBitmap = BitmapFactory.decodeResource(
                requireContext().resources,
                R.drawable.img_default_twibbon
            )

            val bitmapIms = requireActivity().contentResolver.openInputStream(imageUri) ?: return
            val originalPhotoBitmap = BitmapFactory.decodeStream(bitmapIms)
            val rotationMatrix = Matrix()

            when (orientation) {
                6 -> {
                    rotationMatrix.postRotate(90f)
                }

                3 -> {
                    rotationMatrix.postRotate(180f)
                }

                8 -> {
                    rotationMatrix.postRotate(270f)
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                originalPhotoBitmap,
                0,
                0,
                originalPhotoBitmap.width,
                originalPhotoBitmap.height,
                rotationMatrix,
                true
            )

            val flipMatrix = Matrix().apply {
                postScale(
                    -1f,
                    1f,
                    rotatedBitmap.width.toFloat() / 2,
                    rotatedBitmap.height.toFloat()
                )
            }
            val flippedBitmap = Bitmap.createBitmap(
                rotatedBitmap,
                0,
                0,
                rotatedBitmap.width,
                rotatedBitmap.height,
                flipMatrix,
                true
            )

            val scaleMatrix = Matrix().apply {
                val scale = overlayBitmap.width.toFloat() / flippedBitmap.width.toFloat()
                postScale(
                    scale,
                    scale,
                    flippedBitmap.width.toFloat(),
                    flippedBitmap.height.toFloat()
                )
            }

            val photoBitmap = Bitmap.createBitmap(
                flippedBitmap,
                0,
                0,
                flippedBitmap.width,
                flippedBitmap.height,
                scaleMatrix,
                true
            )
            val translateMatrix = Matrix().apply {
                postTranslate(
                    0f,
                    (overlayBitmap.height.toFloat() - photoBitmap.height.toFloat()) / 2f
                )
            }

            val resultBitmap =
                Bitmap.createBitmap(
                    overlayBitmap.width,
                    overlayBitmap.height,
                    overlayBitmap.getConfig()
                )
            val canvas = Canvas(resultBitmap)
            canvas.drawBitmap(photoBitmap, translateMatrix, null)
            canvas.drawBitmap(overlayBitmap, Matrix(), null)

            val bos = ByteArrayOutputStream()
            resultBitmap.compress(CompressFormat.JPEG, 100, bos)
            val bitmapData = bos.toByteArray()

            val f = File(URI(imageUri.toString()))

            val fos = FileOutputStream(f)
            fos.write(bitmapData)
            fos.flush()
            fos.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun deletePreviousTwibbons() {
        requireContext().cacheDir?.apply {
            val files = listFiles() ?: emptyArray()
            files.forEach { file ->
                if (file.name.startsWith("twibbon"))
                    file.delete()
            }
        }
    }

    private fun showPreviewDialog(imageUri: Uri) {
        val dialog = Dialog(requireContext()).apply {
            setCancelable(false)
            setContentView(R.layout.dialog_twibbon_preview)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val imageView = dialog.findViewById<ImageView>(R.id.ivTwibbonPreview)

        Glide.with(requireContext())
            .load(imageUri)
            .into(imageView)

        val closeButton = dialog.findViewById<Button>(R.id.btnCloseTwibbon)

        closeButton.setOnClickListener {
            isProcessingPhoto = false
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun cameraPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermissions() {
        cameraPermissionsLauncher.launch(Manifest.permission.CAMERA)
    }
}