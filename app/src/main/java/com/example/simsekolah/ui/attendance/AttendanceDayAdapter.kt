package com.example.simsekolah.ui.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.databinding.ItemDayScheduleBinding // Reusing the day layout

class AttendanceDayAdapter(
    private val days: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<AttendanceDayAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDayScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size

    inner class ViewHolder(private val binding: ItemDayScheduleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: String) {
            binding.tvDayName.text = day
            binding.ivExpand.setImageResource(android.R.drawable.ic_media_play) // Use as arrow
            binding.root.setOnClickListener { onItemClick(day) }
        }
    }
}
