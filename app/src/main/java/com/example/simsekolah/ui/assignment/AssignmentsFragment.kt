package com.example.simsekolah.ui.assignment

import android.R
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.data.local.room.SekolahDatabase
import com.example.simsekolah.data.remote.retrofit.ApiConfig
import com.example.simsekolah.data.repository.SchoolRepository
import com.example.simsekolah.databinding.DialogAddTugasBinding
import com.example.simsekolah.databinding.FragmentAssignmentsBinding
import com.example.simsekolah.model.SubmissionModel
import com.example.simsekolah.model.TugasModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssignmentsFragment : Fragment() {
    private var _binding: FragmentAssignmentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TugasAdapter
    private var submissionAdapter: SubmissionAdapter? = null
    private val fullTugasList = mutableListOf<TugasModel>() 
    private val displayTugasList = mutableListOf<TugasModel>()
    private val gson = Gson()

    private var selectedFileUri: Uri? = null
    private var selectedFileName: String? = null
    private var currentDialogBinding: DialogAddTugasBinding? = null

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = it.lastPathSegment ?: "selected_file"
            currentDialogBinding?.let { db ->
                db.tvFileName.visibility = View.VISIBLE
                db.tvFileName.text = "Lampiran: $selectedFileName"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAssignmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userPref = UserPreference(requireContext())
        val user = userPref.getUser()

        loadAndFilterTugasData()
        fetchTugasFromApi() // Ambil data dari API juga
        setupRecyclerViews(user.role?.equals("guru", ignoreCase = true) == true)

        if (user.role?.equals("guru", ignoreCase = true) == true) {
            binding.btnAdd.visibility = View.VISIBLE
            binding.layoutSubmissions.visibility = View.VISIBLE
            binding.btnAdd.setOnClickListener {
                showAddTugasDialog(null) // Tambah baru
            }
        } else {
            binding.btnAdd.visibility = View.GONE
            binding.layoutSubmissions.visibility = View.GONE
        }
    }

    private fun fetchTugasFromApi() {
        lifecycleScope.launch {
            try {
                val repository = SchoolRepository.Companion.getInstance(
                    ApiConfig.getApiService(),
                    SekolahDatabase.Companion.getInstance(requireContext()).sekolahDao()
                )
                repository.getTugas().collect { response ->
                    if (response.success) {
                        val apiTasks = response.data.map { item ->
                            TugasModel(
                                id = item.uuid,
                                title = item.namaUjian,
                                description = item.deskripsi,
                                deadline = "${item.deadlineTanggal}-${item.deadlineBulan}-${item.deadlineTahun}",
                                time = item.deadlineJam,
                                teacherId = item.guru?.email,
                                isDone = false
                            )
                        }
                        // Gabungkan dengan data lokal (simulasi)
                        apiTasks.forEach { apiTask ->
                            if (fullTugasList.none { it.id == apiTask.id }) {
                                fullTugasList.add(apiTask)
                            }
                        }
                        saveTugasData()
                        applyFilters()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupRecyclerViews(isGuru: Boolean) {
        taskAdapter = TugasAdapter(
            listTugas = displayTugasList,
            isGuru = isGuru,
            onTugasClicked = { selectedTugas ->
                if (isGuru) {
                    showEditTugasDialog(selectedTugas)
                }
            },
            onDeleteClicked = { tugas ->
                showDeleteConfirmation(tugas)
            }
        )
        binding.rvAssignment.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAssignment.adapter = taskAdapter

        if (isGuru) {
            submissionAdapter = SubmissionAdapter(emptyList())
            binding.rvSubmissions.layoutManager = LinearLayoutManager(requireContext())
            binding.rvSubmissions.adapter = submissionAdapter
        }
    }

    private fun showDeleteConfirmation(tugas: TugasModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Tugas")
            .setMessage("Apakah Anda yakin ingin menghapus tugas '${tugas.title}'?")
            .setPositiveButton("Hapus") { _, _ ->
                fullTugasList.removeAll { it.id == tugas.id }
                saveTugasData()
                applyFilters()
                Toast.makeText(requireContext(), "Tugas berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditTugasDialog(tugas: TugasModel) {
        showAddTugasDialog(tugas)
    }

    private fun showAddTugasDialog(existingTugas: TugasModel?) {
        val dialogBinding = DialogAddTugasBinding.inflate(layoutInflater)
        currentDialogBinding = dialogBinding

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(R.color.transparent)

        // Pre-fill jika edit
        if (existingTugas != null) {
            dialogBinding.etTugasTitle.setText(existingTugas.title)
            dialogBinding.etTugasDeadline.setText(existingTugas.deadline)
            dialogBinding.etTugasTime.setText(existingTugas.time)
            dialogBinding.etTugasDesc.setText(existingTugas.description)
            if (existingTugas.fileName != null) {
                dialogBinding.tvFileName.visibility = View.VISIBLE
                dialogBinding.tvFileName.text = "Lampiran: ${existingTugas.fileName}"
            }
        }

        dialogBinding.btnUploadFile.setOnClickListener {
            pickFileLauncher.launch("*/*")
        }

        dialogBinding.btnCancel.setOnClickListener {
            currentDialogBinding = null
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTugasTitle.text.toString().trim()
            val deadline = dialogBinding.etTugasDeadline.text.toString().trim()
            val time = dialogBinding.etTugasTime.text.toString().trim()
            val desc = dialogBinding.etTugasDesc.text.toString().trim()

            if (title.isEmpty() || deadline.isEmpty() || time.isEmpty()) {
                Toast.makeText(requireContext(), "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userPref = UserPreference(requireContext())
            val currentUser = userPref.getUser()

            if (existingTugas == null) {
                // Tambah baru
                val newTugas = TugasModel(
                    title = title,
                    deadline = deadline,
                    time = time,
                    description = desc,
                    fileName = selectedFileName,
                    filePath = selectedFileUri?.toString(),
                    teacherId = currentUser.email,
                    kelasId = currentUser.age
                )
                fullTugasList.add(0, newTugas)
            } else {
                // Update existing
                val index = fullTugasList.indexOfFirst { it.id == existingTugas.id }
                if (index != -1) {
                    fullTugasList[index] = existingTugas.copy(
                        title = title,
                        deadline = deadline,
                        time = time,
                        description = desc,
                        fileName = selectedFileName ?: existingTugas.fileName,
                        filePath = selectedFileUri?.toString() ?: existingTugas.filePath
                    )
                }
            }

            saveTugasData()
            applyFilters()
            Toast.makeText(requireContext(), "Tugas berhasil disimpan!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveTugasData() {
        val sharedPref = requireActivity().getSharedPreferences("TugasPrefs", Context.MODE_PRIVATE)
        val json = gson.toJson(fullTugasList)
        sharedPref.edit().putString("list_tugas", json).apply()
    }

    private fun loadAndFilterTugasData() {
        val sharedPref = requireActivity().getSharedPreferences("TugasPrefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString("list_tugas", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<TugasModel>>() {}.type
            val savedList: MutableList<TugasModel> = gson.fromJson(json, type)
            fullTugasList.clear()
            fullTugasList.addAll(savedList)
        }
        applyFilters()
    }

    private fun applyFilters() {
        val user = UserPreference(requireContext()).getUser()
        displayTugasList.clear()

        if (user.role?.equals("guru", ignoreCase = true) == true) {
            displayTugasList.addAll(fullTugasList.filter { it.teacherId == user.email })
        } else {
            displayTugasList.addAll(fullTugasList.filter { it.kelasId == user.age })
        }

        if (::taskAdapter.isInitialized) {
            taskAdapter.updateData(displayTugasList)
        }
    }

    override fun onResume() {
        super.onResume()
        loadAndFilterTugasData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentDialogBinding = null
        _binding = null
    }
}
