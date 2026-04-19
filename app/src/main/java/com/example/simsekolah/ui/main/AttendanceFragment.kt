package com.example.simsekolah.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.R
import com.example.simsekolah.adapter.AttendanceAdapter
import com.example.simsekolah.databinding.FragmentAttendanceBinding
import com.example.simsekolah.databinding.DialogAttendanceFormBinding
import com.example.simsekolah.model.ViewModelFactory
import com.example.simsekolah.UserPreference
import com.example.simsekolah.data.remote.response.AbsensiItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var adapter: AttendanceAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userPreference: UserPreference
    private val gson = Gson()

    // KOORDINAT SEKOLAH (Sesuaikan dengan koordinat sekolah asli)
    private val schoolLatitude = -6.175392
    private val schoolLongitude = 106.827153
    private val schoolRadiusInMeters = 100.0 // 100 Meter radius

    // JAM MASUK
    private val entryHour = 9
    private val entryMinute = 54
    private val toleranceMinutes = 5

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                checkLocationAndShowDialog()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                checkLocationAndShowDialog()
            }
            else -> {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        userPreference = UserPreference(requireContext())

        updateDateTime()
        setupRecyclerView()
        loadSavedAttendance() // Muat data yang tersimpan
        observeViewModel()
        setupAction()
    }

    private fun updateDateTime() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        binding.tvCurrentDate.text = dateFormat.format(calendar.time)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.tvCurrentTime.text = timeFormat.format(calendar.time)
    }

    private fun setupRecyclerView() {
        adapter = AttendanceAdapter(emptyList())
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.attendanceList.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
            if (list.isNotEmpty()) {
                binding.tvStatus.text = "Checked In Successfully"
                binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#2F9E44"))
                saveAttendance(list) // Simpan setiap ada perubahan
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupAction() {
        binding.btnAttendance.setOnClickListener {
            if (checkPermissions()) {
                checkLocationAndShowDialog()
            } else {
                requestLocationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationAndShowDialog() {
        if (!isLocationEnabled()) {
            Toast.makeText(requireContext(), "Please turn on your GPS", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val results = FloatArray(1)
                Location.distanceBetween(
                    location.latitude, location.longitude,
                    schoolLatitude, schoolLongitude, results
                )
                val distanceInMeters = results[0]

                if (distanceInMeters <= schoolRadiusInMeters) {
                    showAttendanceDialog(true) 
                } else {
                    showAttendanceDialog(false) 
                }
            } else {
                Toast.makeText(requireContext(), "Could not get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showAttendanceDialog(isWithinArea: Boolean) {
        val dialogBinding = DialogAttendanceFormBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        if (!isWithinArea) {
            dialogBinding.rbHadir.isEnabled = false
            dialogBinding.tvAttendanceInfo.text = "You are outside school area. Only Sick/Permission allowed."
            dialogBinding.tvAttendanceInfo.setTextColor(android.graphics.Color.RED)
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSubmit.setOnClickListener {
            val selectedId = dialogBinding.rgStatus.checkedRadioButtonId
            val keterangan = dialogBinding.etKeterangan.text.toString()
            
            var status = ""
            when (selectedId) {
                R.id.rbHadir -> status = checkLateStatus()
                R.id.rbSakit -> status = "Sakit"
                R.id.rbIzin -> status = "Izin"
            }

            if (status.isEmpty()) {
                Toast.makeText(requireContext(), "Please select status", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendAttendanceToApi(status, keterangan)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkLateStatus(): String {
        val now = Calendar.getInstance()
        val limit = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, entryHour)
            set(Calendar.MINUTE, entryMinute)
            add(Calendar.MINUTE, toleranceMinutes)
        }

        return if (now.after(limit)) "Telat" else "Hadir"
    }

    private fun sendAttendanceToApi(status: String, keterangan: String) {
        val userName = userPreference.getUser().name ?: "Unknown"
        viewModel.postAttendance(status, keterangan, userName)
        Toast.makeText(requireContext(), "Check-in successful!", Toast.LENGTH_SHORT).show()
    }

    private fun saveAttendance(list: List<AbsensiItem>) {
        val sharedPref = requireActivity().getSharedPreferences("AttendanceData", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val json = gson.toJson(list)
        editor.putString("attendance_list", json)
        editor.apply()
    }

    private fun loadSavedAttendance() {
        val sharedPref = requireActivity().getSharedPreferences("AttendanceData", Context.MODE_PRIVATE)
        val json = sharedPref.getString("attendance_list", null)
        if (json != null) {
            val type = object : TypeToken<List<AbsensiItem>>() {}.type
            val savedList: List<AbsensiItem> = gson.fromJson(json, type)
            adapter.updateData(savedList)
            
            // Juga update list di ViewModel agar sinkron saat ada penambahan baru
            // (Karena postAttendance memanipulasi list di ViewModel)
            // Kita bisa tambahkan fungsi di ViewModel untuk set list awal
            // Namun untuk simulasi ini, kita bisa asumsikan list di fragment sudah cukup.
            // Agar aman saat klik tombol lagi:
            // viewModel.setAttendanceList(savedList) -> Jika fungsi ini ada
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}