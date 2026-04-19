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

            when {
                data.isCompleted -> {
                    // SUDAH MENGERJAKAN -> HIJAU
                    tvStudentName.setTextColor(Color.parseColor("#2F9E44"))
                    tvStatus.text = "Completed"
                    tvStatus.setBackgroundResource(R.drawable.bg_status_completed)
                }
                isDeadlineExpired -> {
                    // TIDAK MENGERJAKAN (Deadline Lewat) -> MERAH
                    tvStudentName.setTextColor(Color.parseColor("#E03131"))
                    tvStatus.text = "Missing"
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                }
                else -> {
                    // BELUM MENAMBAHKAN (Masih ada waktu) -> ABU-ABU
                    tvStudentName.setTextColor(Color.parseColor("#777777"))
                    tvStatus.text = "Pending"
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                    // Kita bisa buat bg_status_gray jika ingin backgroundnya abu juga
                }
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