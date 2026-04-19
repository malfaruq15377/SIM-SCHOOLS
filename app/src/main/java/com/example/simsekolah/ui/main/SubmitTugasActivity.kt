package com.example.simsekolah.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.simsekolah.model.TugasModel
import com.example.simsekolah.databinding.ActivitySubmitTugasBinding

class SubmitTugasActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubmitTugasBinding
    private var selectedFileUri: Uri? = null

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            binding.tvFileName.visibility = View.VISIBLE
            binding.tvFileName.text = "Selected File: ${it.lastPathSegment}"
            binding.ivPreview.visibility = View.GONE
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? android.graphics.Bitmap
            imageBitmap?.let {
                binding.ivPreview.visibility = View.VISIBLE
                binding.ivPreview.setImageBitmap(it)
                binding.tvFileName.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmitTugasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tugasData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_TUGAS", TugasModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<TugasModel>("EXTRA_TUGAS")
        }

        tugasData?.let {
            binding.tvSubmitTitle.text = it.title
            binding.tvSubmitDeadline.text = "Deadline: ${it.deadline} - ${it.time}"
            
            // Tampilkan file lampiran dari guru jika ada
            if (!it.fileName.isNullOrEmpty()) {
                binding.cardTeacherFile.visibility = View.VISIBLE
                binding.tvTeacherFileName.text = it.fileName
            }
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }

        binding.btnSelectFile.setOnClickListener {
            pickFileLauncher.launch("*/*") 
        }

        binding.btnTakePhoto.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                takePhotoLauncher.launch(takePictureIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSave.setOnClickListener {
            if (selectedFileUri == null && binding.ivPreview.visibility == View.GONE) {
                Toast.makeText(this, "Please select a file or take a photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            Toast.makeText(this, "Assignment submitted successfully!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}