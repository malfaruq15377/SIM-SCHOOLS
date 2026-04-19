package com.example.simsekolah.ui.dashboard

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.adapter.TugasAdapter
import com.example.simsekolah.UserPreference
import com.example.simsekolah.model.TugasModel
import com.example.simsekolah.databinding.DialogAddTugasBinding
import com.example.simsekolah.databinding.FragmentAssignmentsBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AssignmentsFragment : Fragment() {
    private var _binding: FragmentAssignmentsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: TugasAdapter
    private val tugasList = mutableListOf<TugasModel>()
    private val gson = Gson()
    
    private var selectedFileUri: Uri? = null
    private var selectedFileName: String? = null
    private var currentDialogBinding: DialogAddTugasBinding? = null

    // PERBAIKAN: registerForActivityResult harus dipanggil saat inisialisasi fragment, tidak boleh di dalam onClick
    private val pickFileLauncherForDialog = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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
        
        loadTugasData()
        setupRecyclerView()
        
        val userPref = UserPreference(requireContext())
        val user = userPref.getUser()

        if (user.role == "guru") {
            binding.btnAdd.visibility = View.VISIBLE
            binding.btnAdd.setOnClickListener {
                showAddTugasDialog()
            }
        } else {
            binding.btnAdd.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = TugasAdapter(tugasList)
        binding.rvAssignment.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAssignment.adapter = adapter
    }

    private fun showAddTugasDialog() {
        val dialogBinding = DialogAddTugasBinding.inflate(layoutInflater)
        currentDialogBinding = dialogBinding
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnUploadFile.setOnClickListener {
            // Memanggil launcher yang sudah didaftarkan di atas
            pickFileLauncherForDialog.launch("*/*")
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

            if (title.isEmpty() || deadline.isEmpty()) {
                Toast.makeText(requireContext(), "Judul dan Deadline wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newTugas = TugasModel(
                title = title,
                deadline = deadline,
                time = time,
                description = desc,
                fileName = selectedFileName,
                filePath = selectedFileUri?.toString()
            )

            tugasList.add(0, newTugas)
            saveTugasData()
            adapter.notifyItemInserted(0)
            binding.rvAssignment.scrollToPosition(0)
            
            Toast.makeText(requireContext(), "Tugas baru ditambahkan!", Toast.LENGTH_SHORT).show()
            currentDialogBinding = null
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveTugasData() {
        val sharedPref = requireActivity().getSharedPreferences("TugasPrefs", Context.MODE_PRIVATE)
        val json = gson.toJson(tugasList)
        sharedPref.edit().putString("list_tugas", json).apply()
    }

    private fun loadTugasData() {
        val sharedPref = requireActivity().getSharedPreferences("TugasPrefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString("list_tugas", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<TugasModel>>() {}.type
            val savedList: MutableList<TugasModel> = gson.fromJson(json, type)
            tugasList.clear()
            tugasList.addAll(savedList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentDialogBinding = null
        _binding = null
    }
}