package com.example.simsekolah.ui.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.R
import com.example.simsekolah.data.remote.response.SiswaItem
import com.example.simsekolah.databinding.ItemStudentAttendanceBinding

class StudentMarkingAdapter(private var studentList: List<SiswaItem>) :
    RecyclerView.Adapter<StudentMarkingAdapter.ViewHolder>() {

    data class StudentMarkingItem(
        val name: String,
        val email: String,
        var status: String = "P" // Default Present: P, L, S, I (Izin/Permission)
    )

    class ViewHolder(val binding: ItemStudentAttendanceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentAttendanceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = studentList[position]
        holder.binding.apply {
            tvStudentName.text = student.name
            tvStudentEmail.text = student.email

            // Reset listener to avoid triggering during state restoration
            rgStatus.setOnCheckedChangeListener(null)

            when (student.status) {
                "P" -> rbPresent.isChecked = true
                "L" -> rbLate.isChecked = true
                "S" -> rbSick.isChecked = true
                "I" -> rbPermission.isChecked = true
            }

            rgStatus.setOnCheckedChangeListener { _, checkedId ->
                student.status = when (checkedId) {
                    R.id.rbPresent -> "P"
                    R.id.rbLate -> "L"
                    R.id.rbSick -> "S"
                    R.id.rbPermission -> "I"
                    else -> "P"
                }
            }
        }
    }

    override fun getItemCount(): Int = studentList.size
}
