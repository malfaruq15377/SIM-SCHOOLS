package com.example.simsekolah.ui.fees

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.simsekolah.R

class FeesImageAdapter(private val images: List<Int>) :
    RecyclerView.Adapter<FeesImageAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // PERBAIKAN: ID harus sesuai dengan yang ada di item_fees_image.xml (ivFeesImage)
        // Bukan viewPagerFees (itu ID ViewPager-nya)
        val imageView: ImageView = view.findViewById(R.id.ivFeesImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fees_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageResource(images[position])
    }

    override fun getItemCount(): Int = images.size
}