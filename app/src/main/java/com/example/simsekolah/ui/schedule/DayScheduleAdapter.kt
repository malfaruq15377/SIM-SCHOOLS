package com.example.simsekolah.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.R
import com.example.simsekolah.data.remote.response.JadwalItem
import com.example.simsekolah.databinding.ItemDayScheduleBinding
import com.example.simsekolah.databinding.ItemScheduleRowBinding

class DayScheduleAdapter(
    private var daySchedules: List<DaySchedule>,
    private val isGuru: Boolean = false,
    private val onEditClicked: (DaySchedule) -> Unit = {}
) : RecyclerView.Adapter<DayScheduleAdapter.ViewHolder>() {

    data class DaySchedule(
        val dayName: String,
        val items: List<JadwalItem>?,
        var isExpanded: Boolean = false
    )

    inner class ViewHolder(val binding: ItemDayScheduleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDayScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dayData = daySchedules[position]
        
        with(holder.binding) {
            tvDayName.text = dayData.dayName
            
            // Show edit button only for Guru
            btnEditDay.visibility = if (isGuru) View.VISIBLE else View.GONE
            btnEditDay.setOnClickListener { onEditClicked(dayData) }

            // Setup inner RecyclerView untuk baris jadwal
            val rowAdapter = ScheduleRowAdapter(dayData.items ?: emptyList())
            rvDayItems.layoutManager = LinearLayoutManager(holder.itemView.context)
            rvDayItems.adapter = rowAdapter

            // Handle Expansion
            updateExpansionState(holder, dayData.isExpanded)
            layoutHeader.setOnClickListener {
                dayData.isExpanded = !dayData.isExpanded
                updateExpansionState(holder, dayData.isExpanded)
            }
        }
    }

    private fun updateExpansionState(holder: ViewHolder, isExpanded: Boolean) {
        holder.binding.layoutExpand.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.binding.ivExpand.setImageResource(
            if (isExpanded) R.drawable.ic_minus_circle 
            else R.drawable.ic_plus_circle
        )
    }

    override fun getItemCount(): Int = daySchedules.size

    fun updateData(newData: List<DaySchedule>) {
        daySchedules = newData
        notifyDataSetChanged()
    }
}

class ScheduleRowAdapter(private val items: List<JadwalItem>) :
    RecyclerView.Adapter<ScheduleRowAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemScheduleRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScheduleRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        with(holder.binding) {
            tvRowTime.text = "${item.jamMulai} - ${item.jamSelesai}"
            tvRowSubject.text = item.mapel?.name ?: item.mapelId
            tvRowTeacher.text = item.guru?.nama ?: "-"
        }
    }

    override fun getItemCount(): Int = items.size
}
