package com.example.simsekolah.ui.assignment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.data.remote.response.AssignmentItem
import com.example.simsekolah.databinding.DialogAddTugasBinding
import com.example.simsekolah.databinding.FragmentAssignmentsBinding
import com.example.simsekolah.ui.main.SubmitTugasActivity
import com.example.simsekolah.utils.ViewModelFactory
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class AssignmentsFragment : Fragment() {
    private var _binding: FragmentAssignmentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AssignmentsViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var userPreference: UserPreference
    private lateinit var tugasAdapter: TugasAdapter
    private lateinit var submissionAdapter: SubmissionAdapter
    
    private var userId: Int = 0
    private var userRole: String = ""
    private var userKelasId: Int = 0
    private var selectedFileUri: Uri? = null
    private var addDialog: AlertDialog? = null
    private var dialogBinding: DialogAddTugasBinding? = null

    private val getFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedFileUri = uri
            dialogBinding?.tvFileName?.apply {
                visibility = View.VISIBLE
                text = "Selected: ${uri.lastPathSegment}"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssignmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreference = UserPreference.getInstance(requireContext())
        setupRecyclerView()
        setupUserAccess()
        setupAction()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        tugasAdapter = TugasAdapter { assignment ->
            if (userRole == "guru") {
                binding.tvSelectedTugas.text = "Status Pengumpulan: ${assignment.title}"
                viewModel.loadSubmissions(assignment.id)
            } else {
                showSubmitAssignmentDialog(assignment)
            }
        }
        binding.rvAssignment.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tugasAdapter
        }

        submissionAdapter = SubmissionAdapter { fileUrl ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
            startActivity(intent)
        }
        binding.rvSubmissions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = submissionAdapter
        }
    }

    private fun showSubmitAssignmentDialog(assignment: AssignmentItem) {
        val intent = Intent(requireContext(), SubmitTugasActivity::class.java)
        intent.putExtra("ASSIGNMENT_ID", assignment.id)
        intent.putExtra("SISWA_ID", userId)
        intent.putExtra("ASSIGNMENT_TITLE", assignment.title)
        intent.putExtra("ASSIGNMENT_DEADLINE", assignment.dueDate)
        startActivity(intent)
    }

    private fun setupUserAccess() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = userPreference.getSession().first()
            userId = user.id
            userRole = user.role.lowercase()
            userKelasId = user.kelasId ?: 0

            if (userRole == "guru") {
                binding.btnAdd.visibility = View.VISIBLE
                binding.layoutSubmissions.visibility = View.VISIBLE
                viewModel.loadAssignments(guruId = userId)
                viewModel.loadAllStudents()
            } else {
                binding.btnAdd.visibility = View.GONE
                binding.layoutSubmissions.visibility = View.GONE
                viewModel.loadAssignments(kelasId = userKelasId)
                viewModel.loadStudentSubmissions(userId)
            }
        }
    }

    private fun setupAction() {
        binding.btnAdd.setOnClickListener {
            showAddAssignmentDialog()
        }
    }

    private fun showAddAssignmentDialog() {
        dialogBinding = DialogAddTugasBinding.inflate(layoutInflater)
        addDialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding!!.root)
            .setCancelable(false)
            .create()

        dialogBinding!!.etTugasDeadline.setOnClickListener {
            showDatePicker { date -> dialogBinding!!.etTugasDeadline.setText(date) }
        }

        dialogBinding!!.etTugasTime.setOnClickListener {
            showTimePicker { time -> dialogBinding!!.etTugasTime.setText(time) }
        }

        dialogBinding!!.btnUploadFile.setOnClickListener {
            getFileLauncher.launch("*/*")
        }

        dialogBinding!!.btnCancel.setOnClickListener {
            addDialog?.dismiss()
        }

        dialogBinding!!.btnSave.setOnClickListener {
            val title = dialogBinding!!.etTugasTitle.text.toString()
            val deadlineDate = dialogBinding!!.etTugasDeadline.text.toString()
            val deadlineTime = dialogBinding!!.etTugasTime.text.toString()
            val desc = dialogBinding!!.etTugasDesc.text.toString()

            if (title.isNotEmpty() && deadlineDate.isNotEmpty() && deadlineTime.isNotEmpty()) {
                val fullDeadline = "$deadlineDate $deadlineTime"
                setDialogLoading(true)
                if (selectedFileUri != null) {
                    uploadFileToFirebase(selectedFileUri!!) { downloadUrl ->
                        viewModel.createAssignment(title, desc, fullDeadline, downloadUrl, userId, 1)
                    }
                } else {
                    viewModel.createAssignment(title, desc, fullDeadline, null, userId, 1)
                }
            } else {
                Toast.makeText(requireContext(), "Harap isi semua field", Toast.LENGTH_SHORT).show()
            }
        }
        addDialog?.show()
    }

    private fun setDialogLoading(isLoading: Boolean) {
        dialogBinding?.apply {
            btnSave.isEnabled = !isLoading
            btnCancel.isEnabled = !isLoading
            btnUploadFile.isEnabled = !isLoading
            btnSave.text = if (isLoading) "Saving..." else "Save Assignment"
        }
    }

    private fun uploadFileToFirebase(uri: Uri, onSuccess: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("assignments/${System.currentTimeMillis()}")
        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                setDialogLoading(false)
                Toast.makeText(requireContext(), "Gagal upload file ke Firebase", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(date)
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d:00", hourOfDay, minute)
                onTimeSelected(time)
            },
            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
        ).show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    combine(viewModel.assignments, viewModel.studentSubmissions) { assignments, submissions ->
                        if (userRole == "guru") {
                            assignments
                        } else {
                            val submittedIds = submissions.map { it.assignmentId }
                            assignments.filter { it.id !in submittedIds }
                        }
                    }.collect { tugasAdapter.submitList(it) }
                }
                launch {
                    combine(viewModel.students, viewModel.submissions) { students, submissions ->
                        students.map { it to submissions.find { s -> s.siswaId == it.id } }
                    }.collect { submissionAdapter.submitList(it) }
                }
                launch {
                    viewModel.createStatus.collect { success ->
                        setDialogLoading(false)
                        if (success) {
                            Toast.makeText(requireContext(), "Tugas berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                            addDialog?.dismiss()
                            refreshData()
                        } else {
                            Toast.makeText(requireContext(), "Gagal menambahkan data ke server", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun refreshData() {
        if (userRole == "guru") {
            viewModel.loadAssignments(guruId = userId)
        } else {
            viewModel.loadAssignments(kelasId = userKelasId)
            viewModel.loadStudentSubmissions(userId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialogBinding = null
        addDialog = null
    }
}
