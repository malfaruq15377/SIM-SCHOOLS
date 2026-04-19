package com.example.simsekolah.ui.home

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.R

class CalendarAdapter(
    private val dates: List<Int>,
    private val eventColors: Map<Int, Int> = emptyMap(),
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    var selectedPosition = -1

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_date, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = dates.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = dates[position]
        holder.tvDate.text = date.toString()

        val eventColor = eventColors[date]

        // Reset state ke default
        holder.tvDate.background = null
        holder.tvDate.setBackgroundResource(R.drawable.bg_date)
        holder.tvDate.setTextColor(Color.parseColor("#1A1C1E"))

        if (position == selectedPosition) {
            holder.tvDate.setBackgroundResource(R.drawable.bg_selected)
            holder.tvDate.setTextColor(Color.WHITE)
        } else if (eventColor != null) {
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.OVAL
            shape.setColor(eventColor)
            holder.tvDate.background = shape
            holder.tvDate.setTextColor(Color.WHITE)
        }

        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onClick(date)
        }
    }
}