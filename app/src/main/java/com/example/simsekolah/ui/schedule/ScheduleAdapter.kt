package com.example.simsekolah.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.R
import com.example.simsekolah.databinding.ItemDayScheduleBinding
import com.example.simsekolah.model.ScheduleModel

class ScheduleAdapter(
    private val isGuru: Boolean,
    private val onEditClick: (ScheduleModel) -> Unit
) : ListAdapter<ScheduleModel, ScheduleAdapter.ViewHolder>(DiffCallback) {

    private val expandedStates = mutableMapOf<String, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDayScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemDayScheduleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ScheduleModel) {
            binding.tvDayName.text = item.day
            
            // Setup Nested RecyclerView
            val dayAdapter = DayScheduleAdapter()
            binding.rvDayItems.layoutManager = LinearLayoutManager(itemView.context)
            binding.rvDayItems.adapter = dayAdapter
            dayAdapter.submitList(item.subjects)

            // Role based UI
            binding.btnEditDay.visibility = if (isGuru) View.VISIBLE else View.GONE
            binding.btnEditDay.setOnClickListener { onEditClick(item) }

            // Expand/Collapse Logic
            val isExpanded = expandedStates[item.day] ?: false
            binding.layoutExpand.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.ivExpand.setImageResource(if (isExpanded) R.drawable.ic_minus_circle else R.drawable.ic_plus_circle)

            binding.layoutHeader.setOnClickListener {
                val newState = !isExpanded
                expandedStates[item.day] = newState
                binding.layoutExpand.visibility = if (newState) View.VISIBLE else View.GONE
                binding.ivExpand.setImageResource(if (newState) R.drawable.ic_minus_circle else R.drawable.ic_plus_circle)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ScheduleModel>() {
        override fun areItemsTheSame(oldItem: ScheduleModel, newItem: ScheduleModel): Boolean {
            return oldItem.day == newItem.day
        }

        override fun areContentsTheSame(oldItem: ScheduleModel, newItem: ScheduleModel): Boolean {
            return oldItem == newItem
        }
    }
}
