package com.example.simsekolah.ui.assignment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.data.remote.response.AssignmentItem
import com.example.simsekolah.databinding.ItemTugasBinding

class TugasAdapter(private val onItemClick: (AssignmentItem) -> Unit) : ListAdapter<AssignmentItem, TugasAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemTugasBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AssignmentItem) {
            binding.tvTitle.text = item.title
            binding.tvDeskripsi.text = item.description
            binding.tvTime.text = "Deadline: ${item.dueDate}"
            
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTugasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AssignmentItem>() {
        override fun areItemsTheSame(oldItem: AssignmentItem, newItem: AssignmentItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AssignmentItem, newItem: AssignmentItem): Boolean {
            return oldItem == newItem
        }
    }
}
