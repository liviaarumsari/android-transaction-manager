package com.example.abe.ui.twibbon

import android.Manifest
import android.R.attr
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
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
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.abe.R
import com.example.abe.databinding.FragmentTwibbonBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService


class TwibbonFragment : Fragment() {
    private var _binding: FragmentTwibbonBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

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

        binding.btnCaptureTwibbon.setOnClickListener { previewTwibbon() }
        binding.ibtnCustomTwibbon.setOnClickListener { setCustomTwibbon() }

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
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private val cameraPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission())
        { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(),
                    "Please allow camera to use Twibbon",
                    Toast.LENGTH_SHORT).show()
            }
        }

    private fun previewTwibbon() {
        val imageCapture = imageCapture ?: return
        deletePreviousTwibbons()

        val photoFile = File(
            requireContext().cacheDir,
            "twibbon_${SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireContext(), "Photo capture failed", Toast.LENGTH_SHORT)
                        .show()
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
            Log.d(TAG, "Start overlaying twibbon")
            val ims: InputStream = requireActivity().contentResolver.openInputStream(imageUri) ?: return

            val photoBitmap = BitmapFactory.decodeStream(ims)
            val orientation = ExifInterface(ims).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
            val photoMatrix = Matrix()
            if (orientation == 6) {
                photoMatrix.postRotate(90f)
            } else if (orientation == 3) {
                photoMatrix.postRotate(180f)
            } else if (orientation == 8) {
                photoMatrix.postRotate(270f)
            }

            val overlayBitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.img_default_twibbon)

            val bmOverlay = Bitmap.createBitmap(overlayBitmap.height, overlayBitmap.width, photoBitmap.getConfig())
            val canvas = Canvas(bmOverlay)
            canvas.drawBitmap(photoBitmap, photoMatrix, null)
            canvas.drawBitmap(overlayBitmap, Matrix(), null)

            val bos = ByteArrayOutputStream()
            bmOverlay.compress(CompressFormat.JPEG, 100 /*ignored for PNG*/, bos)
            val bitmapData = bos.toByteArray()

            val f = File(URI(imageUri.toString()))

            val fos = FileOutputStream(f)
            fos.write(bitmapData)
            fos.flush()
            fos.close()

            Log.d(TAG, "Finished overlaying twibbon")
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
        Log.d(TAG, "Previewing twibbon")

        val dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_twibbon_preview)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val imageView = dialog.findViewById<ImageView>(R.id.ivTwibbonPreview)

        Glide.with(requireContext())
            .load(imageUri)
            .into(imageView)

        val closeButton = dialog.findViewById<Button>(R.id.btnCloseTwibbon)

        closeButton.setOnClickListener {
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

    private fun setCustomTwibbon() {
        TODO("Not yet implemented")
    }
}