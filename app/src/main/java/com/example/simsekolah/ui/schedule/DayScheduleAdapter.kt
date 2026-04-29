package com.example.simsekolah.ui.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.databinding.ItemScheduleRowBinding
import com.example.simsekolah.model.SubjectModel

class DayScheduleAdapter : ListAdapter<SubjectModel, DayScheduleAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScheduleRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemScheduleRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SubjectModel) {
            binding.tvRowTime.text = item.time
            binding.tvRowSubject.text = item.name
            binding.tvRowTeacher.text = item.guruName
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SubjectModel>() {
        override fun areItemsTheSame(oldItem: SubjectModel, newItem: SubjectModel): Boolean {
            return oldItem.name == newItem.name && oldItem.time == newItem.time
        }

        override fun areContentsTheSame(oldItem: SubjectModel, newItem: SubjectModel): Boolean {
            return oldItem == newItem
        }
    }
}
