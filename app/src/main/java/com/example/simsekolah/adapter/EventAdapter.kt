package com.example.simsekolah.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.R
import com.example.simsekolah.data.model.EventModel

class EventAdapter(private val eventList: List<EventModel>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayNumber: TextView = view.findViewById(R.id.tvDayNumber)
        val tvMonthName: TextView = view.findViewById(R.id.tvMonthName)
        val tvEventTitle: TextView = view.findViewById(R.id.tvEventTitle)
        val tvEventTime: TextView = view.findViewById(R.id.tvEventTime)
        val tvEventLocation: TextView = view.findViewById(R.id.tvEventLocation)
        val viewIndicator: View = view.findViewById(R.id.viewIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.tvDayNumber.text = event.day
        holder.tvMonthName.text = event.month
        holder.tvEventTitle.text = event.title
        holder.tvEventTime.text = event.time
        holder.tvEventLocation.text = event.location
        
        // Set warna indikator secara dinamis
        if (event.color != 0) {
            holder.viewIndicator.backgroundTintList = ColorStateList.valueOf(event.color)
        }
    }

    override fun getItemCount(): Int = eventList.size
}
