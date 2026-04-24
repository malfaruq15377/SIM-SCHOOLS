package com.example.simsekolah.ui.assignment

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.R
import com.example.simsekolah.databinding.ItemSubmissionStatusBinding
import com.example.simsekolah.model.SubmissionModel

class SubmissionAdapter(
    private var list: List<SubmissionModel>,
    private var isDeadlineExpired: Boolean = false
) : RecyclerView.Adapter<SubmissionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSubmissionStatusBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubmissionStatusBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {
            tvStudentName.text = data.studentName

            if (data.isCompleted) {
                // SUDAH MENGERJAKAN -> BIRU (#4C8DFF)
                tvStudentName.setTextColor(Color.parseColor("#4C8DFF"))
                tvStatus.text = "Completed"
                tvStatus.setBackgroundResource(R.drawable.bg_status_completed)
            } else {
                // BELUM MENGERJAKAN -> MERAH (#FF4C4C)
                tvStudentName.setTextColor(Color.parseColor("#FF4C4C"))
                tvStatus.text = if (isDeadlineExpired) "Missing" else "Pending"
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
            }
        }
    }

    fun updateData(newList: List<SubmissionModel>, expired: Boolean) {
        list = newList
        isDeadlineExpired = expired
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size
}