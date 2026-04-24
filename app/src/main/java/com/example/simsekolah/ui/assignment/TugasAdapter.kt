package com.example.simsekolah.ui.assignment

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.databinding.ItemTugasBinding
import com.example.simsekolah.model.TugasModel
import com.example.simsekolah.ui.main.SubmitTugasActivity

class TugasAdapter(
    private var listTugas: List<TugasModel>,
    private val isGuru: Boolean = false,
    private val onTugasClicked: (TugasModel) -> Unit = {},
    private val onEditClicked: (TugasModel) -> Unit = {},
    private val onDeleteClicked: (TugasModel) -> Unit = {}
) : RecyclerView.Adapter<TugasAdapter.TugasViewHolder>() {

    inner class TugasViewHolder(val binding: ItemTugasBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TugasViewHolder {
        val binding = ItemTugasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TugasViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TugasViewHolder, position: Int) {
        val data = listTugas[position]

        holder.binding.apply {
            tvDeadline.text = "Deadline: ${data.deadline}"
            tvTime.text = data.time
            tvTitle.text = data.title
            tvDeskripsi.text = data.description

            // Tampilan & Fitur khusus Guru
            if (isGuru) {
                containerActions.visibility = View.VISIBLE
                btnEdit.setOnClickListener {
                    onEditClicked(data)
                }
                btnDelete.setOnClickListener {
                    onDeleteClicked(data)
                }

                root.setCardBackgroundColor(Color.WHITE)
                tvTitle.setTextColor(Color.parseColor("#4F46E5"))
            } else {
                containerActions.visibility = View.GONE
            }

            // ACTION KLIK
            root.setOnClickListener {
                if (isGuru) {
                    onTugasClicked(data)
                } else {
                    val intent = Intent(it.context, SubmitTugasActivity::class.java)
                    intent.putExtra("EXTRA_TUGAS", data)
                    it.context.startActivity(intent)
                }
            }
        }
    }

    fun updateData(newList: List<TugasModel>) {
        listTugas = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = listTugas.size
}
