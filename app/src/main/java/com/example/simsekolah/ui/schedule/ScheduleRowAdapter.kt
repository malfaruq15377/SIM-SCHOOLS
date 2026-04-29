package com.example.simsekolah.ui.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.data.remote.response.JadwalItem
import com.example.simsekolah.databinding.ItemScheduleRowBinding

class ScheduleRowAdapter : ListAdapter<JadwalItem, ScheduleRowAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemScheduleRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: JadwalItem) {
            binding.tvRowTime.text = "${item.jamMulai} - ${item.jamSelesai}"
            binding.tvRowSubject.text = item.uuid // Assuming uuid or another field holds subject name if not available
            // Note: JadwalItem usually links to Mapel, you might need to fetch mapel name or use a field
            binding.tvRowTeacher.text = "Guru ID: ${item.guruId}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScheduleRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<JadwalItem>() {
        override fun areItemsTheSame(oldItem: JadwalItem, newItem: JadwalItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: JadwalItem, newItem: JadwalItem): Boolean = oldItem == newItem
    }
}
