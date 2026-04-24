package com.example.simsekolah.ui.assignment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.databinding.DialogAddTugasBinding
import com.example.simsekolah.databinding.FragmentAssignmentsBinding
import com.example.simsekolah.model.SubmissionModel
import com.example.simsekolah.model.TugasModel
import com.example.simsekolah.utils.ViewModelFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AssignmentsFragment : Fragment() {
    private var _binding: FragmentAssignmentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AssignmentsViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var taskAdapter: TugasAdapter
    private lateinit var submissionAdapter: SubmissionAdapter
    private val displayTugasList = mutableListOf<TugasModel>()

    private var assignmentListener: ValueEventListener? = null
    private var submissionListener: ValueEventListener? = null
    private val database = FirebaseDatabase.getInstance("https://simsekolah-68fa2039-default-rtdb.firebaseio.com/").reference

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
        val isGuru = user.role?.equals("guru", ignoreCase = true) == true

        setupRecyclerViews(isGuru)

        if (isGuru) {
            binding.btnAdd.visibility = View.VISIBLE
            binding.btnAdd.setOnClickListener { showAddTugasDialog(null) }
            viewModel.fetchSiswa()

            viewModel.siswaList.observe(viewLifecycleOwner) {
                val currentTugasId = binding.tvSelectedTugas.tag as? String
                if (currentTugasId != null) {
                    refreshSubmissionStatus(currentTugasId)
                }
            }
        } else {
            binding.btnAdd.visibility = View.GONE
            binding.layoutSubmissions.visibility = View.GONE
        }

        listenToRealtimeDatabase(user.role ?: "", user.email ?: "", user.age)
    }

    private fun listenToRealtimeDatabase(role: String, email: String, kelasId: Int) {
        submissionListener = object : ValueEventListener {
            override fun onDataChange(subSnapshot: DataSnapshot) {
                if (_binding == null) return
                refreshTasks(role, email, kelasId, subSnapshot)
                val currentTugasId = binding.tvSelectedTugas.tag as? String
                if (role.equals("guru", ignoreCase = true) && currentTugasId != null) {
                    updateSubmissionUI(currentTugasId, subSnapshot)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("submissions").addValueEventListener(submissionListener!!)

        assignmentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                database.child("submissions").get().addOnSuccessListener { subSnapshot ->
                    refreshTasks(role, email, kelasId, subSnapshot)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("assignments").addValueEventListener(assignmentListener!!)
    }

    private fun refreshTasks(role: String, email: String, kelasId: Int, subSnapshot: DataSnapshot) {
        if (_binding == null || !isAdded) return

        database.child("assignments").get().addOnSuccessListener { snapshot ->
            val newList = mutableListOf<TugasModel>()
            val studentKey = email.replace(".", "_")

            for (data in snapshot.children) {
                val tugas = data.getValue(TugasModel::class.java)
                if (tugas != null) {
                    if (role.equals("guru", ignoreCase = true)) {
                        if (tugas.teacherId == email) newList.add(tugas)
                    } else {
                        val isSubmitted = subSnapshot.child(tugas.id).hasChild(studentKey)
                        if (tugas.kelasId == kelasId && !isSubmitted) {
                            newList.add(tugas)
                        }
                    }
                }
            }

            newList.sortByDescending { it.id }
            displayTugasList.clear()
            displayTugasList.addAll(newList)
            taskAdapter.updateData(displayTugasList.toList())
        }
    }

    private fun setupRecyclerViews(isGuru: Boolean) {
        taskAdapter = TugasAdapter(
            listTugas = displayTugasList,
            isGuru = isGuru,
            onTugasClicked = { selectedTugas ->
                if (isGuru) {
                    loadSubmissionStatus(selectedTugas)
                }
            },
            onEditClicked = { selectedTugas -> showAddTugasDialog(selectedTugas) },
            onDeleteClicked = { tugas ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Hapus Tugas")
                    .setMessage("Hapus tugas ini?")
                    .setPositiveButton("Hapus") { _, _ ->
                        database.child("assignments").child(tugas.id).removeValue()
                        database.child("submissions").child(tugas.id).removeValue()
                    }
                    .setNegativeButton("Batal", null).show()
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

    private fun loadSubmissionStatus(tugas: TugasModel) {
        binding.layoutSubmissions.visibility = View.VISIBLE
        binding.tvSelectedTugas.text = "Status: ${tugas.title}"
        binding.tvSelectedTugas.tag = tugas.id

        binding.scrollView.post {
            binding.scrollView.smoothScrollTo(0, binding.layoutSubmissions.top)
        }
        refreshSubmissionStatus(tugas.id)
    }

    private fun refreshSubmissionStatus(tugasId: String) {
        database.child("submissions").get().addOnSuccessListener { snapshot ->
            if (_binding != null) {
                updateSubmissionUI(tugasId, snapshot)
            }
        }
    }

    private fun updateSubmissionUI(tugasId: String, submissionsSnapshot: DataSnapshot) {
        val students = viewModel.siswaList.value ?: return
        val submissionStatusList = mutableListOf<SubmissionModel>()

        val taskSubmissions = if (submissionsSnapshot.key == tugasId) {
            submissionsSnapshot
        } else {
            submissionsSnapshot.child(tugasId)
        }

        for (student in students) {
            val studentKey = student.email?.replace(".", "_") ?: ""
            val isSubmitted = taskSubmissions.hasChild(studentKey)

            submissionStatusList.add(SubmissionModel(
                studentName = student.nama,
                studentId = student.email ?: "",
                isCompleted = isSubmitted
            ))
        }

        if (::submissionAdapter.isInitialized) {
            submissionAdapter.updateData(submissionStatusList)
        }
    }

    private fun showAddTugasDialog(existingTugas: TugasModel?) {
        val dialogBinding = DialogAddTugasBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogBinding.root).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val calendar = Calendar.getInstance()

        // Date Picker logic
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            dialogBinding.etTugasDeadline.setText(format.format(calendar.time))
        }

        dialogBinding.etTugasDeadline.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Time Picker logic
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            dialogBinding.etTugasTime.setText(format.format(calendar.time))
        }

        dialogBinding.etTugasTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        if (existingTugas != null) {
            dialogBinding.etTugasTitle.setText(existingTugas.title)
            dialogBinding.etTugasDeadline.setText(existingTugas.deadline)
            dialogBinding.etTugasTime.setText(existingTugas.time)
            dialogBinding.etTugasDesc.setText(existingTugas.description)
        }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTugasTitle.text.toString()
            if (title.isEmpty()) {
                Toast.makeText(context, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = UserPreference(requireContext()).getUser()
            val id = existingTugas?.id ?: System.currentTimeMillis().toString()

            val newTugas = TugasModel(
                id = id,
                title = title,
                deadline = dialogBinding.etTugasDeadline.text.toString(),
                time = dialogBinding.etTugasTime.text.toString(),
                description = dialogBinding.etTugasDesc.text.toString(),
                teacherId = user.email ?: "",
                kelasId = user.age,
                isDone = false
            )

            database.child("assignments").child(id).setValue(newTugas)
                .addOnSuccessListener {
                    if (isAdded) {
                        dialog.dismiss()
                        Toast.makeText(context, "Tugas berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        assignmentListener?.let { database.child("assignments").removeEventListener(it) }
        submissionListener?.let { database.child("submissions").removeEventListener(it) }
        _binding = null
    }
}
