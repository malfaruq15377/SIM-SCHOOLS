package com.example.simsekolah.ui.assignment

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.R
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.databinding.DialogAddTugasBinding
import com.example.simsekolah.databinding.FragmentAssignmentsBinding
import com.example.simsekolah.model.TugasModel
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

    private lateinit var taskAdapter: TugasAdapter
    private val displayTugasList = mutableListOf<TugasModel>()
    
    // Gunakan Realtime Database - Sesuaikan URL jika berbeda
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
        listenToRealtimeDatabase(user.role ?: "", user.email ?: "", user.age)

        if (isGuru) {
            binding.btnAdd.visibility = View.VISIBLE
            binding.layoutSubmissions.visibility = View.VISIBLE
            binding.btnAdd.setOnClickListener {
                showAddTugasDialog(null)
            }
        } else {
            binding.btnAdd.visibility = View.GONE
            binding.layoutSubmissions.visibility = View.GONE
        }
    }

    private fun listenToRealtimeDatabase(role: String, email: String, kelasId: Int) {
        database.child("assignments").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val oldSize = displayTugasList.size
                displayTugasList.clear()

                for (data in snapshot.children) {
                    val tugas = data.getValue(TugasModel::class.java)
                    if (tugas != null) {
                        // Filter Logic
                        if (role.equals("guru", ignoreCase = true)) {
                            if (tugas.teacherId == email) displayTugasList.add(tugas)
                        } else {
                            if (tugas.kelasId == kelasId && !tugas.isDone) displayTugasList.add(tugas)
                        }
                    }
                }
                
                displayTugasList.sortByDescending { it.id }
                if (::taskAdapter.isInitialized) {
                    taskAdapter.updateData(displayTugasList)
                }

                // Notifikasi untuk Siswa
                if (!role.equals("guru", ignoreCase = true) && displayTugasList.size > oldSize && oldSize != 0) {
                    showNotification("Tugas Baru!", "Guru Anda telah menambahkan tugas baru")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "assignment_channel"
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Assignments", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.ic_assignment)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun setupRecyclerViews(isGuru: Boolean) {
        taskAdapter = TugasAdapter(
            listTugas = displayTugasList,
            isGuru = isGuru,
            onTugasClicked = { selectedTugas ->
                if (isGuru) showAddTugasDialog(selectedTugas)
            },
            onDeleteClicked = { tugas ->
                database.child("assignments").child(tugas.id).removeValue()
            }
        )
        binding.rvAssignment.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAssignment.adapter = taskAdapter
    }

    private fun showAddTugasDialog(existingTugas: TugasModel?) {
        val dialogBinding = DialogAddTugasBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogBinding.root).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val calendar = Calendar.getInstance()
        if (existingTugas != null) {
            dialogBinding.etTugasTitle.setText(existingTugas.title)
            dialogBinding.etTugasDeadline.setText(existingTugas.deadline)
            dialogBinding.etTugasTime.setText(existingTugas.time)
            dialogBinding.etTugasDesc.setText(existingTugas.description)
        } else {
            dialogBinding.etTugasDeadline.setText(SimpleDateFormat("d-M-yyyy", Locale.getDefault()).format(calendar.time))
            dialogBinding.etTugasTime.setText("23:59")
        }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTugasTitle.text.toString()
            if (title.isEmpty()) return@setOnClickListener

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
                isDone = existingTugas?.isDone ?: false
            )

            database.child("assignments").child(id).setValue(newTugas)
                .addOnSuccessListener {
                    dialog.dismiss()
                    Toast.makeText(context, "Tugas Berhasil Terkirim ke Siswa!", Toast.LENGTH_SHORT).show()
                }
        }
        
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        
        dialogBinding.etTugasDeadline.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                dialogBinding.etTugasDeadline.setText("$d-${m+1}-$y")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        dialogBinding.etTugasTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, h, min ->
                dialogBinding.etTugasTime.setText(String.format("%02d:%02d", h, min))
            }, 23, 59, true).show()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
