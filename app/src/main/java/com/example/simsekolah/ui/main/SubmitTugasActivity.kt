// C:/Users/PC-1/Documents/SIM sekolah/SIM Sekolah/SIM-Sekolah/app/src/main/java/com/example/simsekolah/ui/main/SubmitTugasActivity.kt

package com.example.simsekolah.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.simsekolah.data.local.entity.SubmissionEntity
import com.example.simsekolah.data.repository.SchoolRepository
import com.example.simsekolah.databinding.ActivitySubmitTugasBinding
import com.example.simsekolah.di.Injection
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SubmitTugasActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySubmitTugasBinding
    private lateinit var schoolRepo: SchoolRepository

    private var selectedFileUri: Uri? = null
    private var assignmentId: Int = 0
    private var siswaId: Int = 0

    private val getFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedFileUri = uri
            binding.tvFileName.visibility = View.VISIBLE
            binding.tvFileName.text = "File: ${uri.lastPathSegment}"

            val isImage = contentResolver.getType(uri)?.startsWith("image") == true
            binding.ivPreview.visibility = if (isImage) View.VISIBLE else View.GONE
            if (isImage) binding.ivPreview.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmitTugasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Perbaikan: Gunakan Injection untuk mendapatkan repository
        schoolRepo = Injection.provideRepository(this)

        assignmentId = intent.getIntExtra("ASSIGNMENT_ID", 0)
        siswaId = intent.getIntExtra("SISWA_ID", 0)

        binding.tvSubmitTitle.text = intent.getStringExtra("ASSIGNMENT_TITLE")
        binding.tvSubmitDeadline.text = "Deadline: ${intent.getStringExtra("ASSIGNMENT_DEADLINE")}"

        // Menampilkan file lampiran dari guru jika ada
        val teacherFileUrl = intent.getStringExtra("ASSIGNMENT_FILE_URL")
        if (!teacherFileUrl.isNullOrEmpty()) {
            binding.cardTeacherFile.visibility = View.VISIBLE
            binding.cardTeacherFile.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(teacherFileUrl)))
            }
        }

        binding.btnSelectFile.setOnClickListener { getFileLauncher.launch("*/*") }
        binding.btnTakePhoto.setOnClickListener { getFileLauncher.launch("image/*") }
        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }

        binding.btnSave.setOnClickListener {
            if (selectedFileUri != null) uploadAndSubmit()
            else Toast.makeText(this, "Silakan pilih file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadAndSubmit() {
        setLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val storageRef = FirebaseStorage.getInstance().reference
                    .child("submissions/${siswaId}_${assignmentId}_${System.currentTimeMillis()}")

                storageRef.putFile(selectedFileUri!!).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()

                val submission = SubmissionEntity(
                    assignmentId = assignmentId,
                    siswaId = siswaId,
                    fileUrl = downloadUrl,
                    submittedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                )

                schoolRepo.submitAssignmentFirestore(submission)

                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(this@SubmitTugasActivity, "Berhasil dikirim!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(this@SubmitTugasActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnSave.isEnabled = !isLoading
        binding.btnSave.text = if (isLoading) "Submitting..." else "Submit Now"
    }
}