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
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.simsekolah.R
import com.example.simsekolah.databinding.FragmentProfileBinding
import com.example.simsekolah.ui.form.FormUserActivity
import com.example.simsekolah.model.UserModel
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.ui.settings.SettingActivity
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userModel: UserModel
    private lateinit var mUserPreference: UserPreference
    
    private var isPickingCover = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val fileName = if (isPickingCover) "cover_picture.jpg" else "profile_picture.jpg"
            val savedPath = saveImageToInternalStorage(it, fileName)
            if (savedPath != null) {
                if (isPickingCover) {
                    saveCoverPath(savedPath)
                    loadCoverImage()
                } else {
                    saveProfilePath(savedPath)
                    loadProfileImage()
                }
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
        loadCoverImage()

        binding.btnUpdate.setOnClickListener(this)
        binding.ivSetting.setOnClickListener(this)
        binding.btnBack.setOnClickListener { requireActivity().onBackPressed() }
        
        // Klik foto profil atau icon camera untuk memunculkan pilihan
        binding.ivProfile.setOnClickListener { showProfileOptionsDialog() }
        binding.btnEditPhoto.setOnClickListener { showProfileOptionsDialog() }
        
        // Klik area cover untuk langsung ganti background juga bisa
        binding.ivCover.setOnClickListener {
            isPickingCover = true
            openGallery()
        }
    }

    private fun showProfileOptionsDialog() {
        val options = arrayOf("Lihat Foto Profil", "Ganti Foto Profil", "Ganti Background Cover")
        AlertDialog.Builder(requireContext())
            .setTitle("Opsi Profil")
            .setItems(options) { _, which ->
                when (options[which]) {
                    "Lihat Foto Profil" -> showImageDetail()
                    "Ganti Foto Profil" -> {
                        isPickingCover = false
                        openGallery()
                    }
                    "Ganti Background Cover" -> {
                        isPickingCover = true
                        openGallery()
                    }
                }
            }
            .show()
    }

    private fun populateView(user: UserModel) {
        with(binding) {
            val isGuru = user.role?.equals("guru", ignoreCase = true) == true
            val roleName = if (isGuru) "GURU" else "MURID"
            
            tvDisplayName.text = user.name ?: "No Name"
            tvDisplayRole.text = roleName
            tvDisplayMajor.text = "Class ID: ${user.age}"

            tvStatusBadge.text = roleName
            tvClassBadge.text = user.age.toString()
            tvMajorBadge.text = user.major ?: "-"

            tvNama.text = user.name ?: "No Name"
            tvEmail.text = user.email ?: "No Email"
            
            if (!isGuru) {
                layoutWaliKelas.visibility = View.VISIBLE
                tvWaliKelas.text = user.waliKelasName ?: "Belum Ditentukan"
            } else {
                layoutWaliKelas.visibility = View.GONE
            }
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
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .into(binding.ivProfile)
            }
        }
    }

    private fun loadCoverImage() {
        val savedPath = getCoverPath()
        if (savedPath != null) {
            val file = File(savedPath)
            if (file.exists()) {
                Glide.with(this)
                    .load(file)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.bg_badge_purple)
                    .centerCrop()
                    .into(binding.ivCover)
            }
        }
    }

    private fun showImageDetail() {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image_detail)

        val imageView = dialog.findViewById<ImageView>(R.id.iv_detail)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btn_close)

        val savedPath = getProfilePath()
        if (savedPath != null) {
            val file = File(savedPath)
            if (file.exists()) {
                Glide.with(this).load(file).into(imageView)
            }
        } else {
            imageView.setImageResource(R.drawable.ic_profile)
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

    private fun saveCoverPath(imagePath: String) {
        val sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        sharedPref.edit().putString("cover_path", imagePath).apply()
    }

    private fun getCoverPath(): String? {
        val sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        return sharedPref.getString("cover_path", null)
    }

    private fun saveImageToInternalStorage(uri: Uri, fileName: String): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().filesDir, fileName)
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
            R.id.btn_update -> {
                val intent = Intent(requireContext(), FormUserActivity::class.java)
                intent.putExtra(FormUserActivity.Companion.EXTRA_TYPE_FORM, FormUserActivity.Companion.TYPE_EDIT)
                intent.putExtra(FormUserActivity.Companion.EXTRA_RESULT, userModel)
                resultLauncher.launch(intent)
            }
            R.id.iv_setting -> {
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
