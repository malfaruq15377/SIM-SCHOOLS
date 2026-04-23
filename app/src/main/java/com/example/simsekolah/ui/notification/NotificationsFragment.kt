package com.example.simsekolah.ui.notification

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.databinding.FragmentNotificationsBinding
import com.example.simsekolah.model.NotificationModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val gson = Gson()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val notifList = loadNotifications()
        if (notifList.isEmpty()) {
            binding.tvNoNotif.visibility = View.VISIBLE
            binding.rvNotifications.visibility = View.GONE
        } else {
            binding.tvNoNotif.visibility = View.GONE
            binding.rvNotifications.visibility = View.VISIBLE
            binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
            binding.rvNotifications.adapter = NotificationAdapter(notifList.reversed())
        }
        
        // Tandai semua sebagai terbaca saat halaman dibuka
        markAllAsRead()
    }

    private fun loadNotifications(): List<NotificationModel> {
        val sharedPref = requireActivity().getSharedPreferences("NotifData", Context.MODE_PRIVATE)
        val json = sharedPref.getString("list_notif", null)
        return if (json != null) {
            val type = object : TypeToken<List<NotificationModel>>() {}.type
            gson.fromJson(json, type)
        } else emptyList()
    }

    private fun markAllAsRead() {
        val sharedPref = requireActivity().getSharedPreferences("NotifData", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("has_unread", false).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
