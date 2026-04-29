package com.example.simsekolah.ui.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.R
import com.example.simsekolah.data.remote.response.SiswaItem
import com.example.simsekolah.databinding.ItemStudentAttendanceBinding

class StudentAttendanceAdapter : ListAdapter<SiswaItem, StudentAttendanceAdapter.ViewHolder>(DiffCallback) {

    private val attendanceMap = mutableMapOf<Int, String>() // studentId to status

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = getItem(position)
        // Initialize map with default value if not present
        if (!attendanceMap.containsKey(student.id)) {
            attendanceMap[student.id] = "hadir"
        }
        holder.bind(student)
    }

    fun getAttendanceData(): List<AttendancePostData> {
        return attendanceMap.map { entry ->
            AttendancePostData(
                siswaId = entry.key,
                status = entry.value
            )
        }
    }

    data class AttendancePostData(
        val siswaId: Int,
        val status: String
    )

    inner class ViewHolder(private val binding: ItemStudentAttendanceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(student: SiswaItem) {
            binding.tvStudentName.text = student.name
            binding.tvStudentEmail.text = student.email

            binding.rgStatus.setOnCheckedChangeListener(null)
            
            val currentStatus = attendanceMap[student.id] ?: "hadir"
            when (currentStatus.lowercase()) {
                "hadir" -> binding.rbPresent.isChecked = true
                "late" -> binding.rbLate.isChecked = true
                "sakit" -> binding.rbSick.isChecked = true
                "izin" -> binding.rbPermission.isChecked = true
                else -> binding.rbPresent.isChecked = true
            }

            binding.rgStatus.setOnCheckedChangeListener { _, checkedId ->
                val status = when (checkedId) {
                    R.id.rbPresent -> "hadir"
                    R.id.rbLate -> "late"
                    R.id.rbSick -> "sakit"
                    R.id.rbPermission -> "izin"
                    else -> "hadir"
                }
                attendanceMap[student.id] = status
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SiswaItem>() {
        override fun areItemsTheSame(oldItem: SiswaItem, newItem: SiswaItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SiswaItem, newItem: SiswaItem): Boolean = oldItem == newItem
    }
}
