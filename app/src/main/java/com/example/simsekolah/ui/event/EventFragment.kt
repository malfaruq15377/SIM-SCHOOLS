package com.example.simsekolah.ui.event

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.R
import com.example.simsekolah.ui.home.CalendarAdapter
import com.example.simsekolah.databinding.DialogAddEventBinding
import com.example.simsekolah.model.EventModel
import com.example.simsekolah.receiver.AlarmReceiver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EventFragment : Fragment() {

    private lateinit var rvCalendar: RecyclerView
    private lateinit var rvEvent: RecyclerView
    private lateinit var tvMonth: TextView
    private lateinit var btnPrevMonth: ImageView
    private lateinit var btnNextMonth: ImageView

    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var eventAdapter: EventAdapter
    private val eventList = mutableListOf<EventModel>()

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

        rvCalendar = view.findViewById(R.id.rvCalendar)
        rvEvent = view.findViewById(R.id.rvEvent)
        tvMonth = view.findViewById(R.id.tvMonth)
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)

        loadEvents()
        autoDeletePassedEvents() // Hapus otomatis yang lewat 1 jam
        updateCalendarUI()
        setupEventList()

        btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendarUI()
        }

        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendarUI()
        }

        checkAlarmPermission()
    }

    private fun autoDeletePassedEvents() {
        val now = Calendar.getInstance().timeInMillis
        val oneHourInMillis = 3600000
        val iterator = eventList.iterator()
        var changed = false

        while (iterator.hasNext()) {
            val event = iterator.next()
            val eventTime = getEventTimestamp(event)
            if (eventTime != -1L && (now - eventTime) > oneHourInMillis) {
                iterator.remove()
                // Juga hapus dari map warna jika sudah tidak ada event di hari itu
                removeColorIfNoEvents(event)
                changed = true
            }
        }

        if (changed) {
            saveEvents()
        }
    }

    private fun getEventTimestamp(event: EventModel): Long {
        return try {
            val dateStr = "${event.day} ${event.month} ${calendar.get(Calendar.YEAR)} ${event.time}"
            val format = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault())
            format.parse(dateStr)?.time ?: -1L
        } catch (e: Exception) {
            -1L
        }
    }

    private fun removeColorIfNoEvents(event: EventModel) {
        val dateKey = try {
            val cal = Calendar.getInstance()
            val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            cal.time = format.parse("${event.day} ${event.month} ${calendar.get(Calendar.YEAR)}") ?: Date()
            dateFormat.format(cal.time)
        } catch (e: Exception) { "" }

        val hasOtherEvents = eventList.any {
            val otherDateKey = try {
                val cal = Calendar.getInstance()
                val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                cal.time = format.parse("${it.day} ${it.month} ${calendar.get(Calendar.YEAR)}") ?: Date()
                dateFormat.format(cal.time)
            } catch (e: Exception) { "x" }
            otherDateKey == dateKey
        }

        if (!hasOtherEvents && dateKey.isNotEmpty()) {
            eventColorsMap.remove(dateKey)
        }
    }

    private fun checkAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Permission Required")
                    .setMessage("This app needs permission to set exact alarms for your events. Please enable it in settings.")
                    .setPositiveButton("Settings") { _, _ ->
                        startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun updateCalendarUI() {
        tvMonth.text = monthFormat.format(calendar.time)
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

        calendarAdapter = CalendarAdapter(dates, currentMonthColors) { selectedDate ->
            showAddEventDialog(selectedDate)
        }
        rvCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        rvCalendar.adapter = calendarAdapter
    }

    private fun setupEventList() {
        eventAdapter = EventAdapter(eventList) { eventToDelete ->
            showDeleteConfirmation(eventToDelete)
        }
        rvEvent.layoutManager = LinearLayoutManager(requireContext())
        rvEvent.adapter = eventAdapter
    }

    private fun showDeleteConfirmation(event: EventModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to cancel this event?")
            .setPositiveButton("Yes") { _, _ ->
                cancelAlarm(event)
                eventList.remove(event)
                removeColorIfNoEvents(event)
                saveEvents()
                eventAdapter.updateList(eventList)
                updateCalendarUI()
                Toast.makeText(requireContext(), "Event canceled", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelAlarm(event: EventModel) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val uniqueId = (event.day + event.time + event.month + calendar.get(Calendar.YEAR)).hashCode()
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            uniqueId,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun showAddEventDialog(date: Int) {
        val dialogBinding = DialogAddEventBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
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
                else -> "#1971C2".toColorInt()
            }

            val newEvent = EventModel(
                day = date.toString(),
                month = selectedMonthName,
                title = title,
                time = timeStr,
                location = location,
                color = selectedColor
            )
            setAlarm(date, timeStr, title, location)

            val eventCal = Calendar.getInstance().apply { time = calendar.time; set(Calendar.DAY_OF_MONTH, date) }
            val dateKey = dateFormat.format(eventCal.time)
            eventColorsMap[dateKey] = selectedColor

            eventList.add(0, newEvent)
            saveEvents()
            updateCalendarUI()
            eventAdapter.updateList(eventList)
            rvEvent.scrollToPosition(0)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setAlarm(day: Int, timeStr: String, title: String, location: String) {
        try {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val timeParts = timeStr.split(":")
            val hour = timeParts[0].filter { it.isDigit() }.toIntOrNull() ?: 0
            val minute = timeParts[1].filter { it.isDigit() }.toIntOrNull() ?: 0

            val uniqueId = (day.toString() + timeStr + calendar.get(Calendar.MONTH) + calendar.get(
                Calendar.YEAR)).hashCode()
            val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
                putExtra(AlarmReceiver.Companion.EXTRA_TITLE, "Event Started: $title")
                putExtra(AlarmReceiver.Companion.EXTRA_MESSAGE, "Location: $location at $timeStr")
                putExtra(AlarmReceiver.Companion.EXTRA_ID, uniqueId)
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            }

            val pendingIntent = PendingIntent.getBroadcast(requireContext(), uniqueId, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (alarmCalendar.timeInMillis > System.currentTimeMillis()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val alarmInfo = AlarmManager.AlarmClockInfo(alarmCalendar.timeInMillis, pendingIntent)
                    alarmManager.setAlarmClock(alarmInfo, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmCalendar.timeInMillis, pendingIntent)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
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