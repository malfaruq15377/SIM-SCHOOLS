package com.example.simsekolah.ui.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.data.remote.response.AbsensiItem
import com.example.simsekolah.databinding.ItemTeacherAttendanceBinding

class TeacherAttendanceAdapter(
    private val sessionList: List<AbsensiItem>,
    private val onTakeAttendanceClick: (AbsensiItem) -> Unit
) : RecyclerView.Adapter<TeacherAttendanceAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTeacherAttendanceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTeacherAttendanceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessionList[position]
        holder.binding.apply {
            tvDate.text = session.tanggal
            tvType.text = session.status
            tvDescription.text = session.keterangan

            btnTakeAttendance.setOnClickListener {
                onTakeAttendanceClick(session)
            }
        }
    }

    override fun getItemCount(): Int = sessionList.size
}
