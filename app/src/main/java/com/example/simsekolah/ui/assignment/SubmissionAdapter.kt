package com.example.simsekolah.ui.assignment

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.R
import com.example.simsekolah.data.remote.response.SiswaItem
import com.example.simsekolah.data.remote.response.SubmissionItem
import com.example.simsekolah.databinding.ItemSubmissionStatusBinding

class SubmissionAdapter(private val onDownloadClick: (String) -> Unit) : ListAdapter<Pair<SiswaItem, SubmissionItem?>, SubmissionAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubmissionStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item.first, item.second)
    }

    inner class ViewHolder(private val binding: ItemSubmissionStatusBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(student: SiswaItem, submission: SubmissionItem?) {
            binding.tvStudentName.text = student.name
            if (submission != null) {
                binding.tvStatus.text = "Completed"
                binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
                binding.btnDownload.visibility = View.VISIBLE
                binding.btnDownload.setOnClickListener {
                    onDownloadClick(submission.fileUrl)
                }
            } else {
                binding.tvStatus.text = "Not finished yet"
                binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
                binding.btnDownload.visibility = View.GONE
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Pair<SiswaItem, SubmissionItem?>>() {
        override fun areItemsTheSame(oldItem: Pair<SiswaItem, SubmissionItem?>, newItem: Pair<SiswaItem, SubmissionItem?>): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(oldItem: Pair<SiswaItem, SubmissionItem?>, newItem: Pair<SiswaItem, SubmissionItem?>): Boolean {
            return oldItem == newItem
        }
    }
}
