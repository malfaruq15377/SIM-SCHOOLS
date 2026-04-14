package com.example.simsekolah.data.remote.repository

import com.google.gson.annotations.SerializedName

// Response wrapper for all
data class BaseResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T
)

// Kelas
data class KelasItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("waliKelasId")
    val waliKelasId: String?
)

// Mapel (Subject)
data class MapelItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("kkm")
    val kkm: Int
)

//guru
data class GuruItem(
    @SerializedName("id")
    val id: Any, // Bisa Int atau String tergantung API
    @SerializedName("name") // Sesuaikan dengan API (biasanya 'name' atau 'nama')
    val nama: String,
    @SerializedName("nip")
    val nip: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("password")
    val password: String?
)

// Murid/Siswa (Student)
data class MuridItem(
    @SerializedName("id")
    val id: Any,
    @SerializedName("name")
    val nama: String,
    @SerializedName("email")
    val email: String?,
    @SerializedName("password")
    val password: String?,
    @SerializedName("kelasId")
    val kelasId: Any?
)

// Jadwal (Schedule)
data class JadwalItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("hari")
    val hari: String,
    @SerializedName("jamMulai")
    val jamMulai: String,
    @SerializedName("jamSelesai")
    val jamSelesai: String,
    @SerializedName("mapelId")
    val mapelId: String,
    @SerializedName("guruId")
    val guruId: String,
    @SerializedName("kelasId")
    val kelasId: String,
    @SerializedName("mapel")
    val mapel: MapelItem? = null,
    @SerializedName("guru")
    val guru: GuruItem? = null,
    @SerializedName("kelas")
    val kelas: KelasItem? = null
)

// Nilai (Grade)
data class NilaiItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("muridId")
    val muridId: String,
    @SerializedName("mapelId")
    val mapelId: String,
    @SerializedName("nilai")
    val nilai: Int,
    @SerializedName("tipe")
    val tipe: String // e.g., "UTS", "UAS", "Tugas"
)

// Absensi
data class AbsensiItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("muridId")
    val muridId: String,
    @SerializedName("tanggal")
    val tanggal: String,
    @SerializedName("status")
    val status: String, // e.g., "Hadir", "Izin", "Sakit", "Alpa"
    @SerializedName("keterangan")
    val keterangan: String?
)

// Pengumuman
data class PengumumanItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("judul")
    val judul: String,
    @SerializedName("isi")
    val isi: String,
    @SerializedName("tanggal")
    val tanggal: String,
    @SerializedName("authorId")
    val authorId: String?
)

// Admin
data class AdminItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("nama")
    val nama: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String?
)

// SuperAdmin
data class SuperAdminItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("nama")
    val nama: String,
    @SerializedName("username")
    val username: String
)
