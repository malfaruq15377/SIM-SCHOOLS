package com.example.simsekolah.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.databinding.ItemAttendanceHistoryBinding
import com.example.simsekolah.data.model.AttendanceHistory

class AttendanceAdapter(private val historyList: List<AttendanceHistory>) :
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyList[position]
        holder.binding.apply {
            tvHistoryDate.text = item.date
            tvHistoryTime.text = item.time
            tvHistoryStatus.text = item.status
        }
    }

    override fun getItemCount(): Int = historyList.size
}