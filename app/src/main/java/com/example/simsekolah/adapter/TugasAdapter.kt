package com.example.simsekolah.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.model.TugasModel
import com.example.simsekolah.databinding.ItemTugasBinding
import com.example.simsekolah.ui.main.SubmitTugasActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
            
            root.setOnClickListener {
                // Tandai sebagai SELESAI di memori lokal
                markAsDone(it.context, data.id)
                
                Toast.makeText(it.context, "Tugas '${data.title}' Selesai Dikerjakan!", Toast.LENGTH_SHORT).show()

                val intent = Intent(it.context, SubmitTugasActivity::class.java)
                intent.putExtra("EXTRA_TUGAS", data)
                it.context.startActivity(intent)
            }
        }
    }

    private fun markAsDone(context: Context, tugasId: String) {
        val sharedPref = context.getSharedPreferences("TugasPrefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString("list_tugas", null)
        if (json != null) {
            val gson = Gson()
            val type = object : TypeToken<MutableList<TugasModel>>() {}.type
            val list: MutableList<TugasModel> = gson.fromJson(json, type)
            
            // Cari tugas berdasarkan ID dan ubah statusnya
            list.find { it.id == tugasId }?.isDone = true
            
            sharedPref.edit().putString("list_tugas", gson.toJson(list)).apply()
        }
    }

    override fun getItemCount(): Int = listTugas.size
}
