package com.example.simsekolah.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.data.remote.response.JadwalItem
import com.example.simsekolah.databinding.ItemDayScheduleBinding

class ScheduleAdapter : RecyclerView.Adapter<ScheduleAdapter.DayViewHolder>() {

    private val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    private var schedules: List<JadwalItem> = emptyList()
    private val expandedDays = mutableSetOf<String>()

    fun setSchedules(newList: List<JadwalItem>) {
        schedules = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemDayScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val dayName = days[position]
        holder.bind(dayName)
    }

    override fun getItemCount(): Int = days.size

    inner class DayViewHolder(private val binding: ItemDayScheduleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dayName: String) {
            binding.tvDayName.text = dayName
            
            val isExpanded = expandedDays.contains(dayName)
            binding.layoutExpand.visibility = if (isExpanded) View.VISIBLE else View.GONE
            
            // Filter schedules for this day
            val daySchedules = schedules.filter { it.hari.equals(dayName, ignoreCase = true) }
            
            val rowAdapter = ScheduleRowAdapter()
            binding.rvDayItems.layoutManager = LinearLayoutManager(itemView.context)
            binding.rvDayItems.adapter = rowAdapter
            rowAdapter.submitList(daySchedules)

            binding.layoutHeader.setOnClickListener {
                if (expandedDays.contains(dayName)) {
                    expandedDays.remove(dayName)
                } else {
                    expandedDays.add(dayName)
                }
                notifyItemChanged(adapterPosition)
            }
        }
    }
}
