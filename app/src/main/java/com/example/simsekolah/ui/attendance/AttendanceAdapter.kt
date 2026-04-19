package com.example.simsekolah.ui.attendance

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.data.remote.response.AbsensiItem
import com.example.simsekolah.databinding.ItemAttendanceHistoryBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AttendanceAdapter(private var historyList: List<AbsensiItem>) :
    RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAttendanceHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    fun updateData(newList: List<AbsensiItem>) {
        historyList = newList.sortedByDescending { it.tanggal }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyList[position]
        holder.binding.apply {

            // Format Tanggal dan Jam dari string (Asumsi format: "yyyy-MM-dd HH:mm:ss")
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(item.tanggal)

                if (date != null) {
                    val outDate = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(date)
                    val outTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                    tvHistoryDate.text = outDate
                    tvHistoryTime.text = outTime
                } else {
                    tvHistoryDate.text = item.tanggal
                    tvHistoryTime.text = "--:--"
                }
            } catch (e: Exception) {
                tvHistoryDate.text = item.tanggal
                tvHistoryTime.text = "--:--"
            }

            tvHistoryStatus.text = item.status

            // Parsing Nama dan Keterangan (Format: "NAMA|KETERANGAN")
            val combinedData = item.keterangan ?: ""
            if (combinedData.contains("|")) {
                val parts = combinedData.split("|")
                tvHistoryName.text = parts[0]
                val realKeterangan = if (parts.size > 1) parts[1] else ""

                if (realKeterangan.isNotEmpty()) {
                    tvHistoryDescription.visibility = View.VISIBLE
                    tvHistoryDescription.text = realKeterangan
                } else if (item.status.equals("Telat", ignoreCase = true)) {
                    tvHistoryDescription.visibility = View.VISIBLE
                    tvHistoryDescription.text = "Terlambat Masuk"
                } else {
                    tvHistoryDescription.visibility = View.GONE
                }
            } else {
                tvHistoryName.text = "Student" // Default jika tidak ada data nama
                if (combinedData.isNotEmpty()) {
                    tvHistoryDescription.visibility = View.VISIBLE
                    tvHistoryDescription.text = combinedData
                } else if (item.status.equals("Telat", ignoreCase = true)) {
                    tvHistoryDescription.visibility = View.VISIBLE
                    tvHistoryDescription.text = "Terlambat Masuk"
                } else {
                    tvHistoryDescription.visibility = View.GONE
                }
            }

            // Atur warna indikator berdasarkan status (Update sesuai permintaan: Sakit=Kuning, Hadir=Hijau, Telat=Orange)
            when (item.status.lowercase()) {
                "hadir" -> {
                    viewStatusIndicator.setBackgroundColor(Color.parseColor("#2F9E44")) // Green
                    tvHistoryStatus.setTextColor(Color.parseColor("#2F9E44"))
                }
                "telat" -> {
                    viewStatusIndicator.setBackgroundColor(Color.parseColor("#F08C00")) // Orange
                    tvHistoryStatus.setTextColor(Color.parseColor("#F08C00"))
                }
                "sakit" -> {
                    viewStatusIndicator.setBackgroundColor(Color.parseColor("#FCC419")) // Yellow
                    tvHistoryStatus.setTextColor(Color.parseColor("#FCC419"))
                }
                "izin" -> {
                    viewStatusIndicator.setBackgroundColor(Color.parseColor("#1971C2")) // Blue
                    tvHistoryStatus.setTextColor(Color.parseColor("#1971C2"))
                }
                else -> {
                    viewStatusIndicator.setBackgroundColor(Color.parseColor("#E03131")) // Red
                    tvHistoryStatus.setTextColor(Color.parseColor("#E03131"))
                }
            }
        }
    }

    override fun getItemCount(): Int = historyList.size
}