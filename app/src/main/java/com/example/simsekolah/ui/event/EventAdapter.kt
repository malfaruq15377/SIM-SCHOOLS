package com.example.simsekolah.ui.event

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.R
import com.example.simsekolah.model.EventModel

class EventAdapter(
    private var eventList: List<EventModel>,
    private val isGuru: Boolean = false,
    private val onDeleteClick: (EventModel) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayNumber: TextView = view.findViewById(R.id.tvDayNumber)
        val tvMonthName: TextView = view.findViewById(R.id.tvMonthName)
        val tvEventTitle: TextView = view.findViewById(R.id.tvEventTitle)
        val tvEventTime: TextView = view.findViewById(R.id.tvEventTime)
        val tvEventLocation: TextView = view.findViewById(R.id.tvEventLocation)
        val viewIndicator: View = view.findViewById(R.id.viewIndicator)
        val btnDelete: ImageView = view.findViewById(R.id.btnDeleteEvent)
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

        if (event.color != 0) {
            holder.viewIndicator.backgroundTintList = ColorStateList.valueOf(event.color)
        }

        // Only Guru can see and use delete button
        holder.btnDelete.visibility = if (isGuru) View.VISIBLE else View.GONE
        holder.btnDelete.setOnClickListener {
            onDeleteClick(event)
        }
    }

    override fun getItemCount(): Int = eventList.size

    fun updateList(newList: List<EventModel>) {
        eventList = newList
        notifyDataSetChanged()
    }
}
