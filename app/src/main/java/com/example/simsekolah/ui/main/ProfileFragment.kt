package com.example.simsekolah.ui.main

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.simsekolah.databinding.FragmentProfileBinding
import com.example.simsekolah.ui.form.FormUserActivity
import com.example.simsekolah.model.UserModel
import com.example.simsekolah.UserPreference
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userModel: UserModel
    private lateinit var mUserPreference: UserPreference
    
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val savedPath = saveImageToInternalStorage(it)
            if (savedPath != null) {
                saveProfilePath(savedPath)
                loadProfileImage()
            }
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == FormUserActivity.Companion.RESULT_CODE) {
            val dataIntent = result.data
            if (dataIntent != null) {
                val receivedUser = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    dataIntent.getParcelableExtra(FormUserActivity.Companion.EXTRA_RESULT, UserModel::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    dataIntent.getParcelableExtra<UserModel>(FormUserActivity.Companion.EXTRA_RESULT)
                }

                receivedUser?.let {
                    mUserPreference.setUser(it)
                    userModel = it
                    populateView(it)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mUserPreference = UserPreference(requireContext())
        userModel = mUserPreference.getUser()
        populateView(userModel)
        loadProfileImage()

        binding.btnUpdate.setOnClickListener(this)
        binding.ivSetting.setOnClickListener(this)
        binding.btnBack.setOnClickListener { requireActivity().onBackPressed() }
        binding.ivProfile.setOnClickListener { showImageDetail() }
        binding.btnEditPhoto.setOnClickListener { openGallery() }
    }

    private fun populateView(user: UserModel) {
        with(binding) {
            val roleName = if (user.role == "guru") "GURU" else "MURID"
            
            // Header Info
            tvDisplayName.text = user.name ?: "No Name"
            tvDisplayRole.text = roleName
            tvDisplayMajor.text = "Class ID: ${user.age}" // Menggunakan age sebagai penampung kelasId

            // Stats Badges
            tvStatusBadge.text = roleName
            tvClassBadge.text = user.age.toString()
            tvMajorBadge.text = user.major ?: "-"

            // Personal Info List
            tvNama.text = user.name ?: "No Name"
            tvRoleInfo.text = roleName
            tvClassInfo.text = user.age.toString()
            tvEmail.text = user.email ?: "No Email"
            
            // Other Info
            // tvPhone.text = user.noPhone ?: "-"
            // tvAddress.text = user.address ?: "-"
        }
    }

    private fun loadProfileImage() {
        val savedPath = getProfilePath()
        if (savedPath != null) {
            val file = File(savedPath)
            if (file.exists()) {
                Glide.with(this)
                    .load(file)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(com.example.simsekolah.R.drawable.ic_profile)
                    .circleCrop()
                    .into(binding.ivProfile)
            }
        }
    }

    private fun showImageDetail() {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(com.example.simsekolah.R.layout.dialog_image_detail)

        val imageView = dialog.findViewById<ImageView>(com.example.simsekolah.R.id.iv_detail)
        val btnClose = dialog.findViewById<ImageButton>(com.example.simsekolah.R.id.btn_close)

        val savedPath = getProfilePath()
        if (savedPath != null) {
            val file = File(savedPath)
            if (file.exists()) {
                Glide.with(this).load(file).into(imageView)
            }
        } else {
            imageView.setImageResource(com.example.simsekolah.R.drawable.ic_profile)
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun saveProfilePath(imagePath: String) {
        val sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        sharedPref.edit().putString("profile_path", imagePath).apply()
    }

    private fun getProfilePath(): String? {
        val sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        return sharedPref.getString("profile_path", null)
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().filesDir, "profile_picture.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            com.example.simsekolah.R.id.btn_update -> {
                val intent = Intent(requireContext(), FormUserActivity::class.java)
                intent.putExtra(FormUserActivity.Companion.EXTRA_TYPE_FORM, FormUserActivity.Companion.TYPE_EDIT)
                intent.putExtra(FormUserActivity.Companion.EXTRA_RESULT, userModel)
                resultLauncher.launch(intent)
            }
            com.example.simsekolah.R.id.iv_setting -> {
                val intent = Intent(requireContext(), SettingActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
