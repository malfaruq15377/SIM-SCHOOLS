package com.example.simsekolah.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.data.model.TugasModel
import com.example.simsekolah.databinding.ItemTugasBinding

class TugasAdapter(private val listTugas: List<TugasModel>) :
    RecyclerView.Adapter<TugasAdapter.TugasViewHolder>() {
    
    inner class TugasViewHolder(val binding: ItemTugasBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TugasViewHolder {
        val binding = ItemTugasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TugasViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TugasViewHolder, position: Int) {
        val data = listTugas[position]
        holder.binding.apply {
            tvDeadline.text = data.deadline
            tvTime.text = data.time
            tvTitle.text = data.title
            tvDeskripsi.text = data.description
        }
    }

    override fun getItemCount(): Int = listTugas.size
}
