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
import com.example.simsekolah.model.SubmissionModel
import com.example.simsekolah.model.TugasModel
import com.example.simsekolah.model.UserModel
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
    private lateinit var submissionAdapter: SubmissionAdapter
    private val displayTugasList = mutableListOf<TugasModel>()

    private var assignmentListener: ValueEventListener? = null
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
            binding.btnAdd.setOnClickListener {
                showAddTugasDialog(null)
            }
        } else {
            binding.btnAdd.visibility = View.GONE
            binding.layoutSubmissions.visibility = View.GONE
        }
    }

    private fun listenToRealtimeDatabase(role: String, email: String, kelasId: Int) {
        assignmentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null || !isAdded) return

                val oldSize = displayTugasList.size
                
                // Ambil data pengumpulan user saat ini (untuk filter bagi siswa)
                database.child("submissions").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(subSnapshot: DataSnapshot) {
                        if (_binding == null || !isAdded) return
                        
                        val newList = mutableListOf<TugasModel>()
                        val studentKey = email.replace(".", "_")

                        for (data in snapshot.children) {
                            val tugas = data.getValue(TugasModel::class.java)
                            if (tugas != null) {
                                if (role.equals("guru", ignoreCase = true)) {
                                    if (tugas.teacherId == email) newList.add(tugas)
                                } else {
                                    // Cek apakah siswa ini sudah mengumpulkan tugas ini
                                    val isSubmitted = subSnapshot.child(tugas.id).hasChild(studentKey)
                                    // Hanya tampilkan tugas yang BELUM dikumpulkan (isSubmitted == false)
                                    if (tugas.kelasId == kelasId && !tugas.isDone && !isSubmitted) {
                                        newList.add(tugas)
                                    }
                                }
                            }
                        }

                        newList.sortByDescending { it.id }
                        displayTugasList.clear()
                        displayTugasList.addAll(newList)

                        if (::taskAdapter.isInitialized) {
                            taskAdapter.updateData(displayTugasList.toList())
                        }

                        // Notifikasi untuk Siswa jika ada tugas baru masuk ke list
                        if (!role.equals("guru", ignoreCase = true) && displayTugasList.size > oldSize && oldSize != 0) {
                            showNotification("Tugas Baru!", "Guru Anda telah menambahkan tugas baru")
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        }
        database.child("assignments").addValueEventListener(assignmentListener!!)
    }

    private fun showNotification(title: String, message: String) {
        val ctx = context ?: return
        val channelId = "assignment_channel"
        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Assignments", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(ctx, channelId)
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
                if (isGuru) {
                    loadSubmissionStatus(selectedTugas)
                }
            },
            onEditClicked = { selectedTugas ->
                showAddTugasDialog(selectedTugas)
            },
            onDeleteClicked = { tugas ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Hapus Tugas")
                    .setMessage("Apakah Anda yakin ingin menghapus tugas ini?")
                    .setPositiveButton("Hapus") { _, _ ->
                        database.child("assignments").child(tugas.id).removeValue()
                            .addOnSuccessListener {
                                if (isAdded) Toast.makeText(context, "Tugas berhasil dihapus", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
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
        binding.tvSelectedTugas.text = "Status Pengumpulan: ${tugas.title}"

        binding.scrollView.post {
            binding.scrollView.smoothScrollTo(0, binding.layoutSubmissions.top)
        }

        database.child("users").orderByChild("role").equalTo("siswa").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
                if (_binding == null) return

                val studentsInClass = mutableListOf<UserModel>()
                for (ds in userSnapshot.children) {
                    val user = ds.getValue(UserModel::class.java)
                    if (user != null && user.age == tugas.kelasId) {
                        studentsInClass.add(user)
                    }
                }

                database.child("submissions").child(tugas.id).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(subSnapshot: DataSnapshot) {
                        if (_binding == null) return

                        val submissionStatusList = mutableListOf<SubmissionModel>()

                        for (student in studentsInClass) {
                            val studentKey = student.email?.replace(".", "_") ?: ""
                            val isSubmitted = subSnapshot.hasChild(studentKey)

                            submissionStatusList.add(SubmissionModel(
                                studentName = student.name ?: "Unknown",
                                studentId = student.email ?: "",
                                isCompleted = isSubmitted
                            ))
                        }

                        val sdf = SimpleDateFormat("d-M-yyyy HH:mm", Locale.getDefault())
                        val deadlineStr = "${tugas.deadline} ${tugas.time}"
                        val isExpired = try {
                            val deadlineDate = sdf.parse(deadlineStr)
                            deadlineDate?.before(Calendar.getInstance().time) ?: false
                        } catch (e: Exception) { false }

                        if (::submissionAdapter.isInitialized) {
                            submissionAdapter.updateData(submissionStatusList, isExpired)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showAddTugasDialog(existingTugas: TugasModel?) {
        val dialogBinding = DialogAddTugasBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogBinding.root).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        if (existingTugas != null) {
            dialogBinding.etTugasTitle.setText(existingTugas.title)
            dialogBinding.etTugasDeadline.setText(existingTugas.deadline)
            dialogBinding.etTugasTime.setText(existingTugas.time)
            dialogBinding.etTugasDesc.setText(existingTugas.description)
            dialogBinding.btnSave.text = "Update Tugas"
        } else {
            val calendar = Calendar.getInstance()
            dialogBinding.etTugasDeadline.setText(SimpleDateFormat("d-M-yyyy", Locale.getDefault()).format(calendar.time))
            dialogBinding.etTugasTime.setText("23:59")
        }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTugasTitle.text.toString()
            if (title.isEmpty()) {
                dialogBinding.etTugasTitle.error = "Judul tidak boleh kosong"
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
                isDone = existingTugas?.isDone ?: false
            )

            database.child("assignments").child(id).setValue(newTugas)
                .addOnSuccessListener {
                    if (isAdded) {
                        dialog.dismiss()
                        val msg = if (existingTugas != null) "Tugas Berhasil Diperbarui!" else "Tugas Berhasil Terkirim ke Siswa!"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.etTugasDeadline.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                dialogBinding.etTugasDeadline.setText("$d-${m+1}-$y")
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
        
        dialogBinding.etTugasTime.setOnClickListener {
            val timeParts = dialogBinding.etTugasTime.text.toString().split(":")
            val h = try { timeParts[0].trim().toInt() } catch(e:Exception) { 23 }
            val m = try { timeParts[1].trim().toInt() } catch(e:Exception) { 59 }
            
            TimePickerDialog(requireContext(), { _, hour, min ->
                dialogBinding.etTugasTime.setText(String.format("%02d:%02d", hour, min))
            }, h, m, true).show()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        assignmentListener?.let {
            database.child("assignments").removeEventListener(it)
        }
        _binding = null
    }
}
