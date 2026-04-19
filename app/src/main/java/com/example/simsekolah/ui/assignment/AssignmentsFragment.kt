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
import com.example.simsekolah.ui.assignment.SubmissionAdapter
import com.example.simsekolah.ui.assignment.TugasAdapter
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
    private val fullTugasList = mutableListOf<TugasModel>() // List asli dari Prefs
    private val displayTugasList = mutableListOf<TugasModel>() // List yang sudah difilter
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
        setupRecyclerViews(user.role == "guru")

        if (user.role == "guru") {
            binding.btnAdd.visibility = View.VISIBLE
            binding.layoutSubmissions.visibility = View.VISIBLE
            binding.btnAdd.setOnClickListener {
                showAddTugasDialog(user.age) // user.age = kelasId
            }
            if (displayTugasList.isNotEmpty()) {
                updateSubmissionList(displayTugasList[0])
            }
        } else {
            binding.btnAdd.visibility = View.GONE
            binding.layoutSubmissions.visibility = View.GONE
        }
    }

    private fun setupRecyclerViews(isGuru: Boolean) {
        taskAdapter = TugasAdapter(
            listTugas = displayTugasList,
            isGuru = isGuru,
            onTugasClicked = { selectedTugas ->
                if (isGuru) {
                    updateSubmissionList(selectedTugas)
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
                // Hapus dari list utama (full list)
                fullTugasList.removeAll { it.id == tugas.id }
                saveTugasData()

                // Filter ulang dan update UI
                applyFilters()

                if (displayTugasList.isEmpty()) {
                    binding.tvSelectedTugas.text = "Status Pengumpulan: (Pilih Tugas)"
                    submissionAdapter?.updateData(emptyList(), false)
                } else {
                    updateSubmissionList(displayTugasList[0])
                }

                Toast.makeText(requireContext(), "Tugas berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateSubmissionList(tugas: TugasModel) {
        binding.tvSelectedTugas.text = "Status Pengumpulan: ${tugas.title}"
        val isExpired = checkIfExpired(tugas.deadline, tugas.time)

        val submissions = tugas.submissions ?: emptyList()

        if (submissions.isEmpty()) {
            fetchStudentsForTask(tugas)
        } else {
            submissionAdapter?.updateData(submissions, isExpired)
        }
    }

    private fun fetchStudentsForTask(tugas: TugasModel) {
        val userPref = UserPreference(requireContext())
        val kelasId = userPref.getUser().age

        lifecycleScope.launch {
            val repository = SchoolRepository.Companion.getInstance(
                ApiConfig.getApiService(),
                SekolahDatabase.Companion.getInstance(requireContext()).sekolahDao()
            )

            repository.getSiswa().collect { response ->
                val students = response.data
                    .filter { it.kelasId == kelasId }
                    .take(10)
                    .map {
                        SubmissionModel(
                            studentName = it.nama,
                            studentId = it.id.toString(),
                            isCompleted = false
                        )
                    }

                if (students.isNotEmpty()) {
                    // Update di list utama agar tersimpan permanen
                    val index = fullTugasList.indexOfFirst { it.id == tugas.id }
                    if (index != -1) {
                        fullTugasList[index] = fullTugasList[index].copy(submissions = students)
                        saveTugasData()
                        applyFilters()
                        submissionAdapter?.updateData(students, checkIfExpired(tugas.deadline, tugas.time))
                    }
                }
            }
        }
    }

    private fun checkIfExpired(deadlineDate: String?, deadlineTime: String?): Boolean {
        if (deadlineDate.isNullOrEmpty() || deadlineTime.isNullOrEmpty()) return false
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val deadlineStr = "$deadlineDate $deadlineTime"
            val deadline = format.parse(deadlineStr)
            deadline != null && Date().after(deadline)
        } catch (e: Exception) {
            false
        }
    }

    private fun showAddTugasDialog(kelasId: Int) {
        val dialogBinding = DialogAddTugasBinding.inflate(layoutInflater)
        currentDialogBinding = dialogBinding

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(R.color.transparent)

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

            lifecycleScope.launch {
                val repository = SchoolRepository.Companion.getInstance(
                    ApiConfig.getApiService(),
                    SekolahDatabase.Companion.getInstance(requireContext()).sekolahDao()
                )

                repository.getSiswa().collect { response ->
                    val filteredMurid = response.data
                        .filter { it.kelasId == kelasId }
                        .take(10)
                        .map {
                            SubmissionModel(
                                studentName = it.nama,
                                studentId = it.id.toString(),
                                isCompleted = false
                            )
                        }

                    val currentUser = UserPreference(requireContext()).getUser()

                    val newTugas = TugasModel(
                        title = title,
                        deadline = deadline,
                        time = time,
                        description = desc,
                        fileName = selectedFileName,
                        filePath = selectedFileUri?.toString(),
                        submissions = filteredMurid,
                        teacherId = currentUser.email, // ID Guru pembuat
                        kelasId = kelasId              // ID Kelas tujuan
                    )

                    fullTugasList.add(0, newTugas)
                    saveTugasData()
                    applyFilters()
                    updateSubmissionList(newTugas)
                    binding.rvAssignment.scrollToPosition(0)

                    Toast.makeText(requireContext(), "Tugas dikirim ke kelas Anda!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
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

        if (user.role == "guru") {
            // Guru hanya melihat tugas yang dia buat sendiri
            displayTugasList.addAll(fullTugasList.filter { it.teacherId == user.email })
        } else {
            // Murid hanya melihat tugas yang ditujukan untuk kelasnya
            displayTugasList.addAll(fullTugasList.filter { it.kelasId == user.age }) // user.age = kelasId
        }

        if (::taskAdapter.isInitialized) {
            taskAdapter.updateData(displayTugasList)
        }
    }

    override fun onResume() {
        super.onResume()
        loadAndFilterTugasData()
        val userPref = UserPreference(requireContext())
        if (userPref.getUser().role == "guru" && displayTugasList.isNotEmpty()) {
            updateSubmissionList(displayTugasList[0])
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentDialogBinding = null
        _binding = null
    }
}