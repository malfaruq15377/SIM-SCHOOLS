package com.example.simsekolah.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.R
import com.example.simsekolah.adapter.CalendarAdapter
import com.example.simsekolah.adapter.EventAdapter
import com.example.simsekolah.data.model.EventModel

class EventFragment : Fragment() {

    private lateinit var rvCalendar: RecyclerView
    private lateinit var rvEvent: RecyclerView
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCalendar = view.findViewById(R.id.rvCalendar)
        rvEvent = view.findViewById(R.id.rvEvent)

        setupCalendar()
        setupEventList()
    }

    private fun setupCalendar() {
        val dates = (1..31).toList()
        calendarAdapter = CalendarAdapter(dates) { selectedDate ->
            Toast.makeText(requireContext(), "Klik: $selectedDate", Toast.LENGTH_SHORT).show()
        }

        rvCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        rvCalendar.adapter = calendarAdapter
    }

    private fun setupEventList() {
        val events = listOf(
            EventModel("16", "May", "Outdoor Education Day", "8:30 AM to 4:00 PM", "Masjid Al-Ihya, jl.Marinir"),
            EventModel("18", "May", "Parent Teacher Meeting", "9:00 AM to 12:00 PM", "School Auditorium"),
            EventModel("20", "May", "Sports Day", "7:00 AM to 3:00 PM", "Main Field")
        )

        eventAdapter = EventAdapter(events)
        rvEvent.layoutManager = LinearLayoutManager(requireContext())
        rvEvent.adapter = eventAdapter
    }
}
