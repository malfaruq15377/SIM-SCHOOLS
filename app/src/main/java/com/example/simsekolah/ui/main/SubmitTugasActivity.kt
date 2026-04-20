package com.example.simsekolah.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.model.TugasModel
import com.example.simsekolah.databinding.ActivitySubmitTugasBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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


        tugasData?.let { tugas ->
            binding.tvSubmitTitle.text = tugas.title
            binding.tvSubmitDeadline.text = "Deadline: ${tugas.deadline} - ${tugas.time}"
            
            if (!tugas.fileName.isNullOrEmpty()) {
                binding.cardTeacherFile.visibility = View.VISIBLE
                binding.tvTeacherFileName.text = tugas.fileName
            }

            binding.btnSave.setOnClickListener {
                if (selectedFileUri == null) {
                    Toast.makeText(this, "Please select a file to submit", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                markTaskAsCompleted(tugas.id)
                Toast.makeText(this, "Tugas '${tugas.title}' Berhasil Dikirim!", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnSelectFile.setOnClickListener { pickFileLauncher.launch("*/*") }
    }

    private fun markTaskAsCompleted(tugasId: String) {
        val sharedPref = getSharedPreferences("TugasPrefs", Context.MODE_PRIVATE)
        val userPref = UserPreference(this)
        val currentUser = userPref.getUser()
        val gson = Gson()
        
        val json = sharedPref.getString("list_tugas", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<TugasModel>>() {}.type
            val list: MutableList<TugasModel> = gson.fromJson(json, type)
            
            val tugas = list.find { it.id == tugasId }
            tugas?.let {
                // Update status untuk Murid (lokal)
                it.isDone = true
                
                // Update status di list Guru (Submission)
                val updatedSubmissions = it.submissions.map { sub ->
                    if (sub.studentName == currentUser.name || sub.studentId == currentUser.email) {
                        sub.copy(isCompleted = true, submittedAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()))
                    } else sub
                }
                
                // Simpan kembali
                val index = list.indexOfFirst { t -> t.id == tugasId }
                if (index != -1) {
                    list[index] = it.copy(submissions = updatedSubmissions, isDone = true)
                }
            }
            
            sharedPref.edit().putString("list_tugas", gson.toJson(list)).apply()
        }
    }
}
