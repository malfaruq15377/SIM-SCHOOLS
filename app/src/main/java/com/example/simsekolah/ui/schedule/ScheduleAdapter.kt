package com.example.simsekolah.ui.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.data.remote.response.JadwalItem
import com.example.simsekolah.databinding.ItemScheduleBinding

class ScheduleAdapter(private var listSchedule: List<JadwalItem>) :
    RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemScheduleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    fun updateData(newList: List<JadwalItem>) {
        listSchedule = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listSchedule[position]
        holder.binding.apply {
            tvTime.text = "${item.jamMulai} - ${item.jamSelesai}"
            tvSubject.text = item.mapelId // Idealnya ini nama mapel, perlu join/lookup jika API hanya kirim ID
            tvRoom.text = item.kelasId  // Idealnya ini nama kelas
            tvTeacher.text = item.guruId // Idealnya ini nama guru
        }
    }

    override fun getItemCount(): Int = listSchedule.size
}