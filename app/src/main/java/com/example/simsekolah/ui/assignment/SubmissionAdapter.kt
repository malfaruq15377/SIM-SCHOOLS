package com.example.simsekolah.ui.assignment

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.databinding.ItemSubmissionStatusBinding
import com.example.simsekolah.model.SubmissionModel

class SubmissionAdapter(
    private var list: List<SubmissionModel>
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
                // SUDAH MENGERJAKAN -> completed (Biru)
                tvStatus.text = "completed"
                tvStatus.setTextColor(Color.parseColor("#4C8DFF"))
                viewCircle.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4C8DFF"))
            } else {
                // BELUM MENGERJAKAN -> not finished yet (Merah)
                tvStatus.text = "not finished yet"
                tvStatus.setTextColor(Color.parseColor("#EF4444"))
                viewCircle.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D1D5DB"))
            }
        }
    }

    fun updateData(newList: List<SubmissionModel>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size
}
