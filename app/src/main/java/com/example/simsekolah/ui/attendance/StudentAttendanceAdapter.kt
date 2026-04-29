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
        holder.bind(getItem(position))
    }

    fun getAttendanceData(): Map<Int, String> = attendanceMap

    inner class ViewHolder(private val binding: ItemStudentAttendanceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(student: SiswaItem) {
            binding.tvStudentName.text = student.name
            binding.tvStudentEmail.text = student.email

            // Reset selection to avoid recycling issues
            binding.rgStatus.setOnCheckedChangeListener(null)
            
            val currentStatus = attendanceMap[student.id] ?: "Present"
            when (currentStatus) {
                "Present" -> binding.rbPresent.isChecked = true
                "Late" -> binding.rbLate.isChecked = true
                "Sick" -> binding.rbSick.isChecked = true
                "Permission" -> binding.rbPermission.isChecked = true
            }

            binding.rgStatus.setOnCheckedChangeListener { _, checkedId ->
                val status = when (checkedId) {
                    R.id.rbPresent -> "Present"
                    R.id.rbLate -> "Late"
                    R.id.rbSick -> "Sick"
                    R.id.rbPermission -> "Permission"
                    else -> "Present"
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
