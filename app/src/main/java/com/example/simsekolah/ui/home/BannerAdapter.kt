package com.example.simsekolah.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.data.remote.response.PengumumanItem
import com.example.simsekolah.databinding.ItemBannerBinding

class BannerAdapter : RecyclerView.Adapter<BannerAdapter.ViewHolder>() {

    private val items = mutableListOf<PengumumanItem>()

    fun setItems(newItems: List<PengumumanItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemBannerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PengumumanItem) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
