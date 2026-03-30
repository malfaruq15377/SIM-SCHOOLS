package com.example.simsekolah.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.data.model.Schedule
import com.example.simsekolah.databinding.ItemScheduleBinding

class ScheduleAdapter(private val listSchedule: List<Schedule>) :
    RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemScheduleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listSchedule[position]
        holder.binding.apply {
            tvTime.text = item.time
            tvSubject.text = item.subject
            tvRoom.text = item.room
            tvTeacher.text = item.teacher
        }
    }

    override fun getItemCount(): Int = listSchedule.size
}