package com.example.simsekolah.ui.attendance

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.R
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.data.remote.response.AbsensiItem
import com.example.simsekolah.databinding.DialogAttendanceFormBinding
import com.example.simsekolah.databinding.FragmentAttendanceBinding
import com.example.simsekolah.databinding.FragmentTeacherAttendanceBinding
import com.example.simsekolah.utils.NotificationHelper
import com.example.simsekolah.utils.ViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AttendanceFragment : Fragment() {

    private var _bindingStudent: FragmentAttendanceBinding? = null
    private var _bindingTeacher: FragmentTeacherAttendanceBinding? = null
    
    private val viewModel: AttendanceViewModel by viewModels {
        ViewModelFactory.Companion.getInstance(requireContext())
    }

    private lateinit var userPreference: UserPreference
    private var isGuru: Boolean = false
    private val gson = Gson()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val schoolLatitude = -6.175392
    private val schoolLongitude = 106.827153
    private val schoolRadiusInMeters = 100.0
    
    // KONFIGURASI WAKTU ABSENSI
    private val startHour = 8
    private val startMinute = 0
    private val lateHour = 10
    private val lateMinute = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        userPreference = UserPreference(requireContext())
        isGuru = userPreference.getUser().role?.equals("guru", ignoreCase = true) == true

        return if (isGuru) {
            _bindingTeacher = FragmentTeacherAttendanceBinding.inflate(inflater, container, false)
            _bindingTeacher!!.root
        } else {
            _bindingStudent = FragmentAttendanceBinding.inflate(inflater, container, false)
            _bindingStudent!!.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isGuru) {
            setupTeacherUI()
        } else {
            setupStudentUI()
        }
    }

    private fun setupTeacherUI() {
        val binding = _bindingTeacher!!
        binding.rvTeacherAttendance.layoutManager = LinearLayoutManager(requireContext())
        
        viewModel.sessionDays.observe(viewLifecycleOwner) { days ->
            val sessions = days.mapIndexed { index, date ->
                AbsensiItem(id=index.toString(), muridId="0", tanggal=date, status="All students", keterangan="Regular class session")
            }
            binding.rvTeacherAttendance.adapter = TeacherAttendanceAdapter(sessions) { session ->
                val bundle = Bundle().apply {
                    putString("session_date", session.tanggal)
                }
                findNavController().navigate(R.id.action_attendanceFragment_to_takeAttendanceFragment, bundle)
            }
        }
        viewModel.generateSessionDays()
    }

    private fun setupStudentUI() {
        val binding = _bindingStudent!!
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        updateDateTime()
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        
        viewModel.attendanceList.observe(viewLifecycleOwner) { list ->
            binding.rvHistory.adapter = AttendanceAdapter(list)
            if (list.isNotEmpty()) {
                binding.tvStatus.text = "Absensi Berhasil"
                binding.tvStatus.setTextColor(Color.parseColor("#2F9E44"))
                saveAttendanceLocally(list)
            }
        }

        binding.btnAttendance.setOnClickListener {
            if (isTimeValidForAttendance()) {
                if (checkLocationPermission()) checkLocationAndShowDialog()
                else requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                Toast.makeText(requireContext(), "Absensi hanya dibuka jam 08:00 - 10:00", Toast.LENGTH_LONG).show()
            }
        }
        
        loadSavedAttendance()
        viewModel.fetchAttendance()
    }

    private fun isTimeValidForAttendance(): Boolean {
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        
        val currentTimeInMinutes = currentHour * 60 + currentMinute
        val startTimeInMinutes = startHour * 60 + startMinute
        val lateTimeInMinutes = lateHour * 60 + lateMinute
        
        return currentTimeInMinutes in startTimeInMinutes..lateTimeInMinutes
    }

    private fun updateDateTime() {
        val calendar = Calendar.getInstance()
        _bindingStudent?.tvCurrentDate?.text = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(calendar.time)
        _bindingStudent?.tvCurrentTime?.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    }

    private fun checkLocationPermission() = ActivityCompat.checkSelfPermission(
        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) checkLocationAndShowDialog()
    }

    private fun checkLocationAndShowDialog() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val results = FloatArray(1)
                Location.distanceBetween(location.latitude, location.longitude, schoolLatitude, schoolLongitude, results)
                showAttendanceDialog(results[0] <= schoolRadiusInMeters)
            } else {
                Toast.makeText(requireContext(), "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAttendanceDialog(isWithinArea: Boolean) {
        val dialogBinding = DialogAttendanceFormBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogBinding.root).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        if (!isWithinArea) {
            dialogBinding.rbHadir.isEnabled = false
            dialogBinding.tvAttendanceInfo.text = "Di luar area sekolah. Hanya Sakit/Izin diperbolehkan."
            dialogBinding.tvAttendanceInfo.setTextColor(Color.RED)
        }

        dialogBinding.btnSubmit.setOnClickListener {
            val status = when (dialogBinding.rgStatus.checkedRadioButtonId) {
                R.id.rbHadir -> "Present"
                R.id.rbSakit -> "Sakit"
                R.id.rbIzin -> "Izin"
                else -> ""
            }

            if (status.isNotEmpty()) {
                val user = userPreference.getUser()
                // Gunakan email atau token sebagai ID unik jika ID tidak ada di UserModel
                val userId = user.email ?: "unknown_id"
                val teacherEmail = user.waliKelasName ?: ""
                
                viewModel.postAttendance(status, dialogBinding.etKeterangan.text.toString(), user.name ?: "Siswa", userId, teacherEmail)
                
                // Kirim notifikasi ke sistem
                NotificationHelper.addNotification(
                    requireContext(),
                    "Absensi Berhasil",
                    "Anda telah mengisi absensi status: $status",
                    "absensi"
                )
                
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun saveAttendanceLocally(list: List<AbsensiItem>) {
        val sharedPref = requireActivity().getSharedPreferences("AttendanceData", Context.MODE_PRIVATE)
        sharedPref.edit().putString("attendance_list", gson.toJson(list)).apply()
    }

    private fun loadSavedAttendance() {
        val sharedPref = requireActivity().getSharedPreferences("AttendanceData", Context.MODE_PRIVATE)
        val json = sharedPref.getString("attendance_list", null)
        if (json != null) {
            val savedList: List<AbsensiItem> = gson.fromJson(json, object : TypeToken<List<AbsensiItem>>() {}.type)
            _bindingStudent?.rvHistory?.adapter = AttendanceAdapter(savedList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingStudent = null
        _bindingTeacher = null
    }
}
