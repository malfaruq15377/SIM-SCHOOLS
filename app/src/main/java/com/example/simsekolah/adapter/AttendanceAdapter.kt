package com.example.simsekolah.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.data.remote.repository.AbsensiItem
import com.example.simsekolah.databinding.ItemAttendanceHistoryBinding

class AttendanceAdapter(private var historyList: List<AbsensiItem>) :
    RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAttendanceHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    fun updateData(newList: List<AbsensiItem>) {
        historyList = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyList[position]
        holder.binding.apply {
            // Mapping API data to UI
            tvHistoryDate.text = item.tanggal // Asumsi format tanggal sudah sesuai atau perlu diparsing
            tvHistoryStatus.text = item.status
            tvHistoryTime.text = item.keterangan ?: "-" // Atau sesuaikan dengan field jam jika ada
            
            // Opsional: Ubah warna berdasarkan status
            when (item.status.lowercase()) {
                "hadir" -> tvHistoryStatus.setTextColor(root.context.getColor(android.R.color.holo_green_dark))
                "sakit", "izin" -> tvHistoryStatus.setTextColor(root.context.getColor(android.R.color.holo_orange_dark))
                "alpa" -> tvHistoryStatus.setTextColor(root.context.getColor(android.R.color.holo_red_dark))
            }
        }
    }

    override fun getItemCount(): Int = historyList.size
}
