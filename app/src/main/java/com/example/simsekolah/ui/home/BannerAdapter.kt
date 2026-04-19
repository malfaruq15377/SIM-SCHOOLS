package com.example.simsekolah.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.R

class BannerAdapter(private val listBanner: List<Int>) :
    RecyclerView.Adapter<BannerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgBanner: ImageView = view.findViewById(R.id.imgBanner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listBanner.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imgBanner.setImageResource(listBanner[position])

        holder.itemView.setOnClickListener {
            holder.itemView.animate()
                .scaleX(0.97f)
                .scaleY(0.97f)
                .setDuration(80)
                .withEndAction {
                    holder.itemView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .duration = 80
                }
        }
    }
}