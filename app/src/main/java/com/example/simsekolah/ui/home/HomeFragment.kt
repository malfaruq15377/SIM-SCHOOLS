package com.example.simsekolah.ui.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.simsekolah.R
import com.example.simsekolah.ui.assignment.TugasAdapter
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.databinding.FragmentHomeBinding
import com.example.simsekolah.model.TugasModel
import com.example.simsekolah.ui.main.MainActivity
import com.example.simsekolah.utils.NotificationHelper
import com.google.firebase.database.*
import java.io.File
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreference: UserPreference
    private val tugasList = mutableListOf<TugasModel>()
    private lateinit var adapterTugas: TugasAdapter
    
    private var assignmentListener: ValueEventListener? = null
    private val database = FirebaseDatabase.getInstance("https://simsekolah-68fa2039-default-rtdb.firebaseio.com/").reference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreference = UserPreference(requireContext())
        val user = userPreference.getUser()
        binding.tvUsername.text = if (user.name.isNullOrEmpty()) "User" else user.name

        setupBanner()
        setupMenu()
        loadProfileImage()
        setupRecyclerView()
        
        listenToFirebase(user.role ?: "", user.email ?: "", user.age)
        checkAttendanceReminder()
    }

    private fun setupRecyclerView() {
        val user = userPreference.getUser()
        adapterTugas = TugasAdapter(tugasList, user.role == "guru")
        binding.rvTugas.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterTugas
            isNestedScrollingEnabled = false
        }
    }

    private fun listenToFirebase(role: String, email: String, kelasId: Int) {
        assignmentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null || !isAdded) return

                val oldSize = tugasList.size
                tugasList.clear()

                for (data in snapshot.children) {
                    val tugas = data.getValue(TugasModel::class.java)
                    if (tugas != null) {
                        if (role.equals("guru", ignoreCase = true)) {
                            if (tugas.teacherId == email) tugasList.add(tugas)
                        } else {
                            if (tugas.kelasId == kelasId && !tugas.isDone) tugasList.add(tugas)
                        }
                    }
                }
                
                tugasList.sortByDescending { it.id }
                
                if (tugasList.isEmpty()) {
                    binding.tvNoAssignment.visibility = View.VISIBLE
                    binding.rvTugas.visibility = View.GONE
                } else {
                    binding.tvNoAssignment.visibility = View.GONE
                    binding.rvTugas.visibility = View.VISIBLE
                    adapterTugas.updateData(tugasList)
                }

                if (!role.equals("guru", ignoreCase = true) && tugasList.size > oldSize && oldSize != 0) {
                    val newTugas = tugasList[0]
                    triggerNewAssignmentNotification(newTugas)
                }
                
                updateNotificationBadge()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        }
        database.child("assignments").addValueEventListener(assignmentListener!!)
    }

    private fun triggerNewAssignmentNotification(tugas: TugasModel) {
        NotificationHelper.addNotification(
            requireContext(), 
            "Tugas Baru: ${tugas.title}", 
            "Guru Anda menambahkan tugas baru yang harus dikerjakan.", 
            "tugas"
        )
        showAndroidNotification("Tugas Baru!", "Ada tugas: ${tugas.title}", R.id.assignmentsFragment)
        updateNotificationBadge()
    }

    private fun showAndroidNotification(title: String, message: String, destinationId: Int) {
        val ctx = context ?: return
        val channelId = "school_notification_channel"
        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SIM Sekolah", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Deep Link ke fragment tujuan menggunakan Navigation Component
        val pendingIntent = NavDeepLinkBuilder(ctx)
            .setGraph(R.navigation.nav_main)
            .setDestination(destinationId)
            .setComponentName(MainActivity::class.java)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun updateNotificationBadge() {
        if (_binding == null || !isAdded) return
        if (NotificationHelper.hasUnread(requireContext())) {
            binding.notificationBadge.visibility = View.VISIBLE
        } else {
            binding.notificationBadge.visibility = View.GONE
        }
    }

    private fun checkAttendanceReminder() {
        val user = userPreference.getUser()
        if (user.role == "guru") return
        val now = Calendar.getInstance()
        // Pengecekan sederhana: setiap hari jam 07:50
        if (now.get(Calendar.HOUR_OF_DAY) == 7 && now.get(Calendar.MINUTE) == 50) {
            val pref = requireActivity().getSharedPreferences("AttendanceReminder", Context.MODE_PRIVATE)
            val lastDate = pref.getString("last_remind", "")
            val today = "${now.get(Calendar.YEAR)}-${now.get(Calendar.MONTH)}-${now.get(Calendar.DAY_OF_MONTH)}"
            
            if (lastDate != today) {
                NotificationHelper.addNotification(requireContext(), "Pengingat Absen", "10 menit lagi masuk nih!", "absensi")
                showAndroidNotification("Pengingat Absen", "Jangan lupa absen hari ini ya!", R.id.attendanceFragment)
                pref.edit().putString("last_remind", today).apply()
                updateNotificationBadge()
            }
        }
    }

    private fun loadProfileImage() {
        val sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val savedPath = sharedPref.getString("profile_path", null)
        if (savedPath != null) {
            val file = File(savedPath)
            if (file.exists()) {
                Glide.with(this).load(file).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.ic_profile).circleCrop().into(binding.ivProfile)
            }
        }
    }

    private fun setupBanner() {
        val bannerList = listOf(R.drawable.banner1, R.drawable.banner2, R.drawable.banner3)
        binding.viewPagerBanner.adapter = BannerAdapter(bannerList)
        binding.dotsIndicator.attachTo(binding.viewPagerBanner)
    }

    private fun setupMenu() {
        binding.menuAssignments.setOnClickListener { findNavController().navigate(R.id.assignmentsFragment) }
        binding.menuEvent.setOnClickListener { findNavController().navigate(R.id.eventFragment) }
        binding.menuInformation.setOnClickListener { findNavController().navigate(R.id.feesFragment) }
        binding.ivProfile.setOnClickListener { findNavController().navigate(R.id.profileFragment) }
        binding.btnNotification.setOnClickListener { findNavController().navigate(R.id.action_homeFragment_to_notificationsFragment) }
    }

    override fun onResume() {
        super.onResume()
        updateNotificationBadge()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        assignmentListener?.let {
            database.child("assignments").removeEventListener(it)
        }
        _binding = null
    }
}
