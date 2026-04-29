package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @field:SerializedName("success")
    val success: Boolean,
    @field:SerializedName("message")
    val message: String,
    @field:SerializedName("data")
    val data: T
)

data class GuruItem(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("uuid") val uuid: String,
    @field:SerializedName("nip") val nip: String,
    @field:SerializedName("name") val name: String,
    @field:SerializedName("email") val email: String,
    @field:SerializedName("phone") val phone: String,
    @field:SerializedName("address") val address: String,
    @field:SerializedName("gender") val gender: String,
    @field:SerializedName("birthDate") val birthDate: String,
    @field:SerializedName("isWaliKelas") val isWaliKelas: Boolean,
    @field:SerializedName("status") val status: String,
    @field:SerializedName("isActive") val isActive: Boolean
)

data class SiswaItem(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("uuid") val uuid: String,
    @field:SerializedName("nis") val nis: String,
    @field:SerializedName("name") val name: String,
    @field:SerializedName("email") val email: String,
    @field:SerializedName("phone") val phone: String,
    @field:SerializedName("address") val address: String,
    @field:SerializedName("gender") val gender: String,
    @field:SerializedName("birthDate") val birthDate: String,
    @field:SerializedName("parentName") val parentName: String,
    @field:SerializedName("kelasId") val kelasId: Int,
    @field:SerializedName("status") val status: String,
    @field:SerializedName("isActive") val isActive: Boolean
)

data class JadwalItem(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("uuid") val uuid: String,
    @field:SerializedName("hari") val hari: String,
    @field:SerializedName("jamMulai") val jamMulai: String,
    @field:SerializedName("jamSelesai") val jamSelesai: String,
    @field:SerializedName("ruangan") val ruangan: String,
    @field:SerializedName("tipe") val tipe: String,
    @field:SerializedName("mapelId") val mapelId: Int,
    @field:SerializedName("kelasId") val kelasId: Int,
    @field:SerializedName("guruId") val guruId: Int,
    @field:SerializedName("status") val status: String
)

data class MapelItem(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("uuid") val uuid: String,
    @field:SerializedName("kodeMapel") val kodeMapel: String,
    @field:SerializedName("name") val name: String,
    @field:SerializedName("kategori") val kategori: String,
    @field:SerializedName("deskripsi") val deskripsi: String,
    @field:SerializedName("guruId") val guruId: Int,
    @field:SerializedName("kelasId") val kelasId: Int
)

data class NilaiItem(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("uuid") val uuid: String,
    @field:SerializedName("siswaId") val siswaId: Int,
    @field:SerializedName("mapelId") val mapelId: Int,
    @field:SerializedName("guruId") val guruId: Int,
    @field:SerializedName("kelasId") val kelasId: Int,
    @field:SerializedName("nilai") val nilai: String?,
    @field:SerializedName("jenis") val jenis: String,
    @field:SerializedName("semester") val semester: String,
    @field:SerializedName("tahunAjaran") val tahunAjaran: String,
    @field:SerializedName("bobot") val bobot: Int,
    @field:SerializedName("catatan") val catatan: String?
)

data class PengumumanItem(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("uuid") val uuid: String,
    @field:SerializedName("title") val title: String,
    @field:SerializedName("content") val content: String,
    @field:SerializedName("target") val target: String,
    @field:SerializedName("priority") val priority: String,
    @field:SerializedName("publishAt") val publishAt: String,
    @field:SerializedName("status") val status: String
)

data class AbsensiItem(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("uuid") val uuid: String,
    @field:SerializedName("siswaId") val siswaId: Int,
    @field:SerializedName("tanggal") val tanggal: String,
    @field:SerializedName("jamMasuk") val jamMasuk: String?,
    @field:SerializedName("jamKeluar") val jamKeluar: String?,
    @field:SerializedName("status") val status: String,
    @field:SerializedName("metode") val metode: String,
    @field:SerializedName("keterangan") val keterangan: String?,
    @field:SerializedName("divalidasi") val divalidasi: Boolean
)

data class KelasItem(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("uuid") val uuid: String,
    @field:SerializedName("name") val name: String,
    @field:SerializedName("tingkat") val tingkat: String,
    @field:SerializedName("jurusan") val jurusan: String,
    @field:SerializedName("kapasitas") val kapasitas: Int,
    @field:SerializedName("jumlahSiswa") val jumlahSiswa: Int,
    @field:SerializedName("status") val status: String,
    @field:SerializedName("waliKelasId") val waliKelasId: Int,
    @field:SerializedName("createdAt") val createdAt: String,
    @field:SerializedName("updatedAt") val updatedAt: String
)

data class AssignmentItem(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("title") val title: String,
    @field:SerializedName("description") val description: String,
    @field:SerializedName("dueDate") val dueDate: String,
    @field:SerializedName("fileUrl") val fileUrl: String?,
    @field:SerializedName("guruId") val guruId: Int,
    @field:SerializedName("kelasId") val kelasId: Int
)

data class SubmissionItem(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("assignmentId") val assignmentId: Int,
    @field:SerializedName("siswaId") val siswaId: Int,
    @field:SerializedName("fileUrl") val fileUrl: String,
    @field:SerializedName("submittedAt") val submittedAt: String
)
