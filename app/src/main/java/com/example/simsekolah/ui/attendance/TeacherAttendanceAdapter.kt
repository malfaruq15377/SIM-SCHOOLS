package com.example.simsekolah.ui.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.data.remote.response.AbsensiItem
import com.example.simsekolah.databinding.ItemTeacherAttendanceBinding

class TeacherAttendanceAdapter(
    private val list: List<AbsensiItem>,
    private val onItemClick: (AbsensiItem) -> Unit
) : RecyclerView.Adapter<TeacherAttendanceAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTeacherAttendanceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTeacherAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvStudentName.text = item.tanggal // Sementara gunakan field tanggal sebagai label atau nama
        
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = list.size
}