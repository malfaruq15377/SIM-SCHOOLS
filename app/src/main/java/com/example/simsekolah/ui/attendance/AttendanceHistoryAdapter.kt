package com.example.simsekolah.ui.attendance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.R
import com.example.simsekolah.data.remote.response.AbsensiItem
import com.example.simsekolah.databinding.ItemAttendanceHistoryBinding

class AttendanceHistoryAdapter : ListAdapter<AbsensiItem, AttendanceHistoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemAttendanceHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AbsensiItem) {
            binding.tvHistoryName.text = "Absensi Siswa" // Or get from item if available
            binding.tvHistoryDate.text = item.tanggal
            binding.tvHistoryStatus.text = item.status.replaceFirstChar { it.uppercase() }
            binding.tvHistoryTime.text = item.jamMasuk ?: "--:--"
            
            if (!item.keterangan.isNullOrEmpty()) {
                binding.tvHistoryDescription.text = item.keterangan
                binding.tvHistoryDescription.visibility = View.VISIBLE
            } else {
                binding.tvHistoryDescription.visibility = View.GONE
            }

            val color = when (item.status.lowercase()) {
                "hadir" -> R.color.md_theme_light_primary
                "sakit" -> android.R.color.holo_orange_light
                "izin" -> android.R.color.holo_blue_light
                "alpha" -> android.R.color.holo_red_light
                else -> R.color.md_theme_light_primary
            }
            binding.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(itemView.context, color))
            binding.tvHistoryStatus.setTextColor(ContextCompat.getColor(itemView.context, color))
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AbsensiItem>() {
            override fun areItemsTheSame(oldItem: AbsensiItem, newItem: AbsensiItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: AbsensiItem, newItem: AbsensiItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
