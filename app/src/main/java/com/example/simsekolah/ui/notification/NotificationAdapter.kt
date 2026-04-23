package com.example.simsekolah.ui.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.databinding.ItemNotificationBinding
import com.example.simsekolah.model.NotificationModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(private var list: List<NotificationModel>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {
            tvNotifTitle.text = data.title
            tvNotifMessage.text = data.message
            tvNotifTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(data.timestamp))
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<NotificationModel>) {
        list = newList
        notifyDataSetChanged()
    }
}
