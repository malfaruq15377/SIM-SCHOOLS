package com.example.simsekolah.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.simsekolah.R
import com.example.simsekolah.databinding.FragmentProfileBinding
import com.example.simsekolah.utils.UserModel
import com.example.simsekolah.utils.UserPreference
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var userModel: UserModel
    private lateinit var mUserPreference: UserPreference
    private var imageUri: Uri? = null
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // 1. Tampilkan ke ImageView menggunakan Glide
                Glide.with(this).load(it).into(binding.ivProfile)

                // 2. Simpan file asli ke storage internal aplikasi
                val savedPath = saveImageToInternalStorage(it)

                // 3. Simpan path lokasinya ke SharedPreferences agar permanen
                if (savedPath != null) {
                    saveProfilePath(savedPath)
                }
            }
        }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == FormUserActivity.RESULT_CODE) {
            val dataIntent = result.data
            if (dataIntent != null) {
                val receivedUser = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    dataIntent.getParcelableExtra(FormUserActivity.EXTRA_RESULT, UserModel::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    dataIntent.getParcelableExtra<UserModel>(FormUserActivity.EXTRA_RESULT)
                }

                receivedUser?.let {
                    // Simpan ke SharedPreferences agar data permanen
                    mUserPreference.setUser(it)
                    userModel = it
                    populateView(it)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mUserPreference = UserPreference(requireContext())

        // Load data awal dari SharedPreferences
        userModel = mUserPreference.getUser()
        populateView(userModel)

        binding.btnUpdate.setOnClickListener(this)

        binding.ivProfile.setOnClickListener {
            openGallery()
        }



        val savedPath = getProfilePath()
        if (savedPath != null) {
            val file = File(savedPath)
            if (file.exists()) {
                Glide.with(this)
                    .load(file)
                    .placeholder(R.drawable.ic_profile) // Gambar default jika loading
                    .into(binding.ivProfile)
            }
        }
    }

    private fun populateView(userModel: UserModel) {
        with(binding) {
            tvNama.text = if (userModel.name.isNullOrEmpty()) "No Name" else userModel.name
            tvEmail.text = if (userModel.email.isNullOrEmpty()) "No Email" else userModel.email
            tvPhone.text = if (userModel.noPhone.isNullOrEmpty()) "No Phone" else userModel.noPhone
            tvAddress.text = if (userModel.address.isNullOrEmpty()) "No Address" else userModel.address
            tvAge.text = if (userModel.age == 0) "0" else userModel.age.toString()
            tvHeight.text = if (userModel.height == 0.0) "0" else userModel.height.toString()
            tvWeight.text = if (userModel.weight == 0.0) "0" else userModel.weight.toString()
            tvMajor.text = if (userModel.major.isNullOrEmpty()) "No Major" else userModel.major
            tvFather.text = if (userModel.fatherName.isNullOrEmpty()) "No Father" else userModel.fatherName
            tvMother.text = if (userModel.motherName.isNullOrEmpty()) "No Mother" else userModel.motherName

            tvDisplayName.text = tvNama.text
            tvDisplayMajor.text = tvMajor.text
        }
    }

    private fun saveProfilePath(imagePath: String) {
        val sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("profile_path", imagePath)
            apply()
        }
    }

    private fun getProfilePath(): String? {
        val sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        return sharedPref.getString("profile_path", null)
    }
    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileName = "profile_picture.jpg"
            val file = File(requireContext().filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            file.absolutePath // Mengembalikan lokasi file yang baru disimpan
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_update) {
            val intent = Intent(requireContext(), FormUserActivity::class.java)
            intent.putExtra(FormUserActivity.EXTRA_TYPE_FORM, FormUserActivity.TYPE_EDIT)
            intent.putExtra(FormUserActivity.EXTRA_RESULT, userModel)
            resultLauncher.launch(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}