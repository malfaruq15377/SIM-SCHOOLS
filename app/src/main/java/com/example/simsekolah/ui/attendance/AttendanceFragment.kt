package com.example.simsekolah.ui.attendance

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.R
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.databinding.DialogAttendanceFormBinding
import com.example.simsekolah.databinding.FragmentAttendanceBinding
import com.example.simsekolah.utils.ViewModelFactory
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AttendanceFragment : Fragment() {
    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var userPreference: UserPreference
    private var userRole: String = ""
    private var userId: Int = 0
    private lateinit var historyAdapter: AttendanceHistoryAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val targetLat = -6.386997
    private val targetLon = 106.777376
    private val maxDistanceMeter = 100.0 

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                // Permission granted
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreference = UserPreference.getInstance(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        setupRecyclerView()
        setupUIByRole()
        setupClock()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        historyAdapter = AttendanceHistoryAdapter()
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun setupUIByRole() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = userPreference.getSession().first()
            userRole = user.role.lowercase()
            userId = user.id

            if (userRole == "guru") {
                setupTeacherView()
            } else {
                setupStudentView()
                viewModel.loadAttendanceHistory(userId)
            }
        }
    }

    private fun setupTeacherView() {
        binding.tvTitle.text = "Teacher Attendance"
        binding.btnAttendance.visibility = View.GONE
        binding.tvLocation.visibility = View.GONE
        binding.cardInfo.visibility = View.GONE
        binding.tvHistoryHeader.text = "Attendance Days"
        
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        binding.rvHistory.adapter = AttendanceDayAdapter(days) { 
            findNavController().navigate(R.id.takeAttendanceFragment)
        }
    }

    private fun setupStudentView() {
        if (!checkPermission()) {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }

        binding.btnAttendance.setOnClickListener {
            if (checkPermission()) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val results = FloatArray(1)
                        Location.distanceBetween(location.latitude, location.longitude, targetLat, targetLon, results)
                        showAttendanceDialog(results[0] <= maxDistanceMeter)
                    } else {
                        requestNewLocationDataAndShowDialog()
                    }
                }
            } else {
                requestPermissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        }
    }

    private fun checkPermission() = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun requestNewLocationDataAndShowDialog() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMaxUpdates(1)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                if (lastLocation != null) {
                    val results = FloatArray(1)
                    Location.distanceBetween(lastLocation.latitude, lastLocation.longitude, targetLat, targetLon, results)
                    showAttendanceDialog(results[0] <= maxDistanceMeter)
                } else {
                    Toast.makeText(requireContext(), "Gagal mendapatkan lokasi. Aktifkan GPS!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun showAttendanceDialog(isInRange: Boolean) {
        val dialogBinding = DialogAttendanceFormBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogBinding.root).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // LOGIC: Disable Present if out of range
        if (!isInRange) {
            dialogBinding.rbHadir.isEnabled = false
            dialogBinding.rbHadir.alpha = 0.5f
            dialogBinding.tvAttendanceInfo.text = "Anda diluar jangkauan sekolah. Hanya bisa Izin atau Sakit."
            dialogBinding.rbIzin.isChecked = true // Auto select permission
        } else {
            dialogBinding.rbHadir.isEnabled = true
            dialogBinding.rbHadir.alpha = 1.0f
            dialogBinding.tvAttendanceInfo.text = "Silakan pilih status kehadiran Anda."
            dialogBinding.rbHadir.isChecked = true
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSubmit.setOnClickListener {
            val status = when (dialogBinding.rgStatus.checkedRadioButtonId) {
                R.id.rbHadir -> "hadir"
                R.id.rbSakit -> "sakit"
                R.id.rbIzin -> "izin"
                else -> "hadir"
            }
            
            if (status == "hadir" && !isInRange) {
                Toast.makeText(requireContext(), "Gagal: Anda harus di area sekolah untuk Present", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val keterangan = dialogBinding.etKeterangan.text.toString()
            val tanggal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val jamMasuk = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

            viewModel.submitAttendance(userId, status, tanggal, if (status == "hadir") jamMasuk else null, keterangan.ifEmpty { status }, "manual")
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.attendanceResult.collect { result ->
                result.onSuccess {
                    Toast.makeText(requireContext(), "Absensi berhasil dikirim!", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        lifecycleScope.launch {
            viewModel.attendanceHistory.collect { history ->
                historyAdapter.submitList(history)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val todayAttendance = history.find { it.tanggal == today }
                if (todayAttendance != null) {
                    binding.tvStatus.text = todayAttendance.status.replaceFirstChar { it.uppercase() }
                    binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_light_primary))
                }
            }
        }
        lifecycleScope.launch {
            viewModel.isLoading.collect { binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE }
        }
    }

    private fun setupClock() {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateSdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        binding.tvCurrentTime.text = sdf.format(Date())
        binding.tvCurrentDate.text = dateSdf.format(Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
