package com.example.simsekolah.ui.information

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.simsekolah.databinding.FragmentInformasiBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InformasiFragment : Fragment() {

    private var _binding: FragmentInformasiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentInformasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDatePicker()
        setupAgeListener()
    }

    private fun setupDatePicker() {
        binding.etBirth.isFocusable = false
        binding.etBirth.isClickable = true
        binding.etBirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)

                    val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    binding.etBirth.setText(format.format(selectedDate.time))

                    calculateAge(selectedYear, selectedMonth, selectedDay)
                },
                year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun setupAgeListener() {
        // Juga handle jika user input manual dengan format yang benar (opsional)
        binding.etBirth.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.length == 10) { // format dd-mm-yyyy
                    try {
                        val parts = input.split("-")
                        if (parts.size == 3) {
                            val day = parts[0].toInt()
                            val month = parts[1].toInt() - 1 // Calendar month is 0-based
                            val year = parts[2].toInt()
                            calculateAge(year, month, day)
                        }
                    } catch (e: Exception) {
                        // ignore invalid format
                    }
                }
            }
        })
    }

    private fun calculateAge(year: Int, month: Int, day: Int) {
        val today = Calendar.getInstance()
        val birthDate = Calendar.getInstance()
        birthDate.set(year, month, day)

        var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        if (age >= 0) {
            binding.etAge.setText(age.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}