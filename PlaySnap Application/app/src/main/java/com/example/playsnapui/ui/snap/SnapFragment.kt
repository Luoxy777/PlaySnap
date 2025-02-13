package com.example.playsnapui.ui.snap

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentSnapBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class SnapFragment : Fragment() {
    private var binding: FragmentSnapBinding? = null
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var cameraSelector: CameraSelector? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private val aspectRatio = AspectRatio.RATIO_16_9

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSnapBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val galleryThumbnail = view.findViewById<ImageView>(R.id.gallery_thumbnail)
        val photoCountText = view.findViewById<TextView>(R.id.photo_count_text)

        deleteAllCapturedPhotos()
        updatePhotoCount(view.findViewById(R.id.photo_count_text))

        if (checkMultiplePermission()) {
            startCamera()
        }

        binding!!.btnFlipCamera.setOnClickListener {
            lensFacing =
                if (lensFacing == CameraSelector.LENS_FACING_FRONT) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
            bindCameraUserCases()
        }
        binding!!.cameraButton.setOnClickListener { takePhoto() }
        binding!!.btnFlash.setOnClickListener { setFlashIcon(camera) }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_SnapFragment_to_HomeFragment)
        }

        binding!!.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_SnapFragment_to_HomeFragment)
        }

        binding!!.mulaiButton.setOnClickListener {
            findNavController().navigate(R.id.action_SnapFragment_to_ScrollGalleryFragment)
        }
    }

    private fun checkMultiplePermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                14
            )
            return false
        }
        return true
    }

    private fun startCamera() {
        ProcessCameraProvider.getInstance(requireContext()).addListener({
            try {
                cameraProvider = ProcessCameraProvider.getInstance(requireContext()).get()
                bindCameraUserCases()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUserCases() {
        if (cameraProvider == null) return
        val rotation = binding!!.previewView.display.rotation
        val preview = Preview.Builder()
            .setTargetAspectRatio(aspectRatio)
            .setTargetRotation(rotation)
            .build()
        preview.surfaceProvider = binding!!.previewView.surfaceProvider
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetAspectRatio(aspectRatio)
            .setTargetRotation(rotation)
            .build()
        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
        cameraProvider!!.unbindAll()
        camera = cameraProvider!!.bindToLifecycle(this, cameraSelector!!, preview, imageCapture)
    }

    private fun setFlashIcon(camera: Camera?) {
        if (camera!!.cameraInfo.hasFlashUnit()) {
            val isTorchOn = camera.cameraInfo.torchState.value == TorchState.ON
            camera.cameraControl.enableTorch(!isTorchOn)
        } else {
            Toast.makeText(requireContext(), "Flash is Not Available", Toast.LENGTH_LONG).show()
            binding!!.btnFlash.isEnabled = false
        }
    }

    private fun takePhoto() {
        if (imageCapture == null) return
        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Images")
            }
        }
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()
        imageCapture!!.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(requireContext(), "Photo Capture Succeeded", Toast.LENGTH_LONG).show()
                    loadLastCapturedImage(binding!!.galleryThumbnail)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(requireContext(), exception.message, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun loadLastCapturedImage(imageView: ImageView) {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val cursor: Cursor? = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, sortOrder
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val imagePath = it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                Glide.with(this).load(File(imagePath)).centerCrop().into(imageView)
            }
        }
        updatePhotoCount(view?.findViewById(R.id.photo_count_text)!!)
    }

    private fun updatePhotoCount(textView: TextView) {
        val cursor: Cursor? = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            null, null, null
        )
        val count = cursor?.count ?: 0
        cursor?.close()
        textView.text = count.toString()
    }

    private fun deleteAllCapturedPhotos() {
        val resolver = requireContext().contentResolver
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        resolver.delete(uri, null, null)
    }
}
