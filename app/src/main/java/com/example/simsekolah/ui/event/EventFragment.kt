package com.example.simsekolah.ui.event

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.R
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.ui.home.CalendarAdapter
import com.example.simsekolah.databinding.DialogAddEventBinding
import com.example.simsekolah.model.EventModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EventFragment : Fragment() {

    private var rvCalendar: RecyclerView? = null
    private var rvEvent: RecyclerView? = null
    private var tvMonth: TextView? = null
    private var btnPrevMonth: ImageView? = null
    private var btnNextMonth: ImageView? = null

    private var calendarAdapter: CalendarAdapter? = null
    private var eventAdapter: EventAdapter? = null
    private val eventList = mutableListOf<EventModel>()
    private var userPreference: UserPreference? = null

    private var eventColorsMap = mutableMapOf<String, Int>()

    private var calendar = Calendar.getInstance()
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference(requireContext())
        
        rvCalendar = view.findViewById(R.id.rvCalendar)
        rvEvent = view.findViewById(R.id.rvEvent)
        tvMonth = view.findViewById(R.id.tvMonth)
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)

        loadEvents()
        updateCalendarUI()
        setupEventList()
        startAutoSync()

        btnPrevMonth?.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendarUI()
        }

        btnNextMonth?.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendarUI()
        }
    }

    private fun startAutoSync() {
        lifecycleScope.launch {
            while (true) {
                val oldSize = eventList.size
                loadEvents()
                if (eventList.size > oldSize && oldSize != 0) {
                    val user = userPreference?.getUser()
                    if (user?.role?.equals("guru", ignoreCase = true) == false) {
                        showNotification("Event Baru!", "Sekolah menambahkan event baru")
                        updateCalendarUI()
                        eventAdapter?.updateList(eventList)
                    }
                }
                delay(5000)
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "event_channel"
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Events", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.ic_event)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun updateCalendarUI() {
        tvMonth?.text = monthFormat.format(calendar.time)
        val dates = mutableListOf<Int>()
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..maxDay) { dates.add(i) }

        val currentMonthColors = mutableMapOf<Int, Int>()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        eventColorsMap.forEach { (dateStr, color) ->
            try {
                val eventCal = Calendar.getInstance()
                eventCal.time = dateFormat.parse(dateStr) ?: Date()
                if (eventCal.get(Calendar.YEAR) == currentYear && eventCal.get(Calendar.MONTH) == currentMonth) {
                    currentMonthColors[eventCal.get(Calendar.DAY_OF_MONTH)] = color
                }
            } catch (e: Exception) {}
        }

        val isGuru = userPreference?.getUser()?.role?.equals("guru", ignoreCase = true) == true
        calendarAdapter = CalendarAdapter(dates, currentMonthColors) { selectedDate ->
            if (isGuru) {
                showAddEventDialog(selectedDate)
            } else {
                Toast.makeText(requireContext(), "Hanya Guru yang dapat menambah event", Toast.LENGTH_SHORT).show()
            }
        }
        
        rvCalendar?.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
        }
    }

    private fun setupEventList() {
        val isGuru = userPreference?.getUser()?.role?.equals("guru", ignoreCase = true) == true
        eventAdapter = EventAdapter(eventList, isGuru) { eventToDelete ->
            showDeleteConfirmation(eventToDelete)
        }
        rvEvent?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }

    private fun showDeleteConfirmation(event: EventModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete this event?")
            .setPositiveButton("Yes") { _, _ ->
                eventList.remove(event)
                saveEvents()
                eventAdapter?.updateList(eventList)
                updateCalendarUI()
                Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showAddEventDialog(date: Int) {
        val dialogBinding = DialogAddEventBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val selectedMonthName = monthFormat.format(calendar.time).split(" ")[0]
        val selectedYear = calendar.get(Calendar.YEAR)
        dialogBinding.tvSelectedDate.text = "$selectedMonthName $date, $selectedYear"

        dialogBinding.etEventTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val time = String.format("%02d:%02d", hour, minute)
                dialogBinding.etEventTime.setText(time)
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etEventTitle.text.toString().trim()
            val timeStr = dialogBinding.etEventTime.text.toString().trim()
            val location = dialogBinding.etEventLocation.text.toString().trim()

            if (title.isEmpty() || timeStr.isEmpty()) {
                Toast.makeText(requireContext(), "Fill title and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedColor = when (dialogBinding.rgColor.checkedRadioButtonId) {
                R.id.rbRed -> "#E03131".toColorInt()
                R.id.rbGreen -> "#2F9E44".toColorInt()
                R.id.rbOrange -> "#F08C00".toColorInt()
                else -> "#4F46E5".toColorInt()
            }

            val newEvent = EventModel(
                day = date.toString(),
                month = selectedMonthName,
                title = title,
                time = timeStr,
                location = location,
                color = selectedColor
            )

            val eventCal = Calendar.getInstance().apply { 
                time = calendar.time
                set(Calendar.DAY_OF_MONTH, date) 
            }
            val dateKey = dateFormat.format(eventCal.time)
            eventColorsMap[dateKey] = selectedColor

            eventList.add(0, newEvent)
            saveEvents()
            updateCalendarUI()
            eventAdapter?.updateList(eventList)
            rvEvent?.scrollToPosition(0)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun saveEvents() {
        val sharedPref = requireActivity().getSharedPreferences("EventData", Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putString("event_list", gson.toJson(eventList))
            putString("event_colors", gson.toJson(eventColorsMap))
            apply()
        }
    }

    private fun loadEvents() {
        val sharedPref = requireActivity().getSharedPreferences("EventData", Context.MODE_PRIVATE)
        val eventListJson = sharedPref.getString("event_list", null)
        val eventColorsJson = sharedPref.getString("event_colors", null)
        if (eventListJson != null) {
            val type = object : TypeToken<MutableList<EventModel>>() {}.type
            eventList.clear(); eventList.addAll(gson.fromJson(eventListJson, type))
        }
        if (eventColorsJson != null) {
            val type = object : TypeToken<MutableMap<String, Int>>() {}.type
            eventColorsMap.clear(); eventColorsMap.putAll(gson.fromJson(eventColorsJson, type))
        }
    }
}
