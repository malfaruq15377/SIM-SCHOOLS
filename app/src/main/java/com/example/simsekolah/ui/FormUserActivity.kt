package com.example.simsekolah.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.simsekolah.R
import com.example.simsekolah.databinding.ActivityFormUserBinding
import com.example.simsekolah.utils.UserModel
import java.util.Calendar

class FormUserActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityFormUserBinding

    companion object {
        const val EXTRA_TYPE_FORM = "extra_type_form"
        const val EXTRA_RESULT = "extra_result"
        const val RESULT_CODE = 110

        const val TYPE_ADD = 1
        const val TYPE_EDIT = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnChange.setOnClickListener(this)

        val typeForm = intent.getIntExtra(EXTRA_TYPE_FORM, 0)
        val userModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_RESULT, UserModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<UserModel>(EXTRA_RESULT)
        }

        val actionBarTitle: String
        val btnTitle: String

        if (typeForm == TYPE_EDIT) {
            actionBarTitle = "Ubah"
            btnTitle = "Update"
            userModel?.let { showEditForm(it) }
        } else {
            actionBarTitle = "Tambah Baru"
            btnTitle = "Simpan"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnChange.text = btnTitle
        binding.etBirth.setOnClickListener {
            showDatePickerDialog()
            }
        }

        private fun showDatePickerDialog() {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    binding.etBirth.setText(date)
                },
                year, month, day
            )
            datePickerDialog.show()
        }

    private fun showEditForm(userModel: UserModel) {
        with(binding) {
            etName.setText(userModel.name)
            etEmail.setText(userModel.email)
            etAge.setText(userModel.age.toString())
            etPhone.setText(userModel.noPhone)
            etAddress.setText(userModel.address)
            etMajor.setText(userModel.major)
            etFatherName.setText(userModel.fatherName)
            etMotherName.setText(userModel.motherName)
            etHeight.setText(userModel.height.toString())
            etWeight.setText(userModel.weight.toString())
            etBirth.setText(userModel.dateOfBirth)
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_change) {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val age = binding.etAge.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val major = binding.etMajor.text.toString().trim()
            val father = binding.etFatherName.text.toString().trim()
            val mother = binding.etMotherName.text.toString().trim()
            val height = binding.etHeight.text.toString().trim()
            val weight = binding.etWeight.text.toString().trim()
            val birth = binding.etBirth.text.toString().trim()

            if (name.isEmpty()) {
                binding.etName.error = "Field tidak boleh kosong"
                return
            }

            val userModel = UserModel(
                name = name,
                email = email,
                address = address,
                major = major,
                fatherName = father,
                motherName = mother,
                height = if (height.isEmpty()) 0.0 else height.toDouble(),
                weight = if (weight.isEmpty()) 0.0 else weight.toDouble(),
                dateOfBirth = birth,
                noPhone = phone,
                age = if (age.isEmpty()) 0 else age.toInt()
            )

            val resultIntent = Intent()
            resultIntent.putExtra(EXTRA_RESULT, userModel)
            setResult(RESULT_CODE, resultIntent)

            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
