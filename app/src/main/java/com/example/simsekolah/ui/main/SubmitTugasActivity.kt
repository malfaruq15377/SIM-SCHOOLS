package com.example.simsekolah.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.model.TugasModel
import com.example.simsekolah.databinding.ActivitySubmitTugasBinding
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SubmitTugasActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubmitTugasBinding
    private var selectedFileUri: Uri? = null
    private var photoUri: Uri? = null
    
    private val database = FirebaseDatabase.getInstance("https://simsekolah-68fa2039-default-rtdb.firebaseio.com/").reference

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            selectedFileUri = photoUri
            binding.ivPreview.visibility = View.VISIBLE
            binding.ivPreview.setImageURI(selectedFileUri)
            binding.tvFileName.visibility = View.VISIBLE
            binding.tvFileName.text = "Foto Tugas Berhasil Diambil"
        }
    }

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            binding.tvFileName.visibility = View.VISIBLE
            binding.tvFileName.text = "File Terpilih: ${it.lastPathSegment}"
            binding.ivPreview.visibility = View.GONE
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openCamera()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmitTugasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userPref = UserPreference(this)
        val user = userPref.getUser()

        val tugasData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_TUGAS", TugasModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<TugasModel>("EXTRA_TUGAS")
        }

        tugasData?.let { tugas ->
            binding.tvSubmitTitle.text = tugas.title
            binding.tvSubmitDeadline.text = "Deadline: ${tugas.deadline} - ${tugas.time}"
            
            binding.btnSave.setOnClickListener {
                if (selectedFileUri == null) {
                    Toast.makeText(this, "Lampirkan file atau foto tugas", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                val studentEmail = user.email ?: ""
                if (studentEmail.isNotEmpty()) {
                    submitTugas(tugas.id, studentEmail)
                } else {
                    Toast.makeText(this, "User email tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnSelectFile.setOnClickListener { pickFileLauncher.launch("*/*") }
        binding.ivPreview.setOnClickListener { 
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        try {
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val photoFile = File.createTempFile(
                "IMG_${System.currentTimeMillis()}_",
                ".jpg",
                storageDir
            )
            photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            photoUri?.let {
                cameraLauncher.launch(it)
            } ?: run {
                Toast.makeText(this, "Gagal membuat URI foto", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Gagal menyiapkan file foto: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitTugas(tugasId: String, studentEmail: String) {
        val studentKey = studentEmail.replace(".", "_")
        val submissionData = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "status" to "completed"
        )

        // Simpan ke node submissions/{tugasId}/{studentKey}
        database.child("submissions").child(tugasId).child(studentKey).setValue(submissionData)
            .addOnSuccessListener {
                Toast.makeText(this, "Tugas Berhasil Dikumpulkan!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengumpulkan tugas", Toast.LENGTH_SHORT).show()
            }
    }
}
