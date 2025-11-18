package com.app.unetclass.features.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.unetclass.R
import com.bumptech.glide.Glide

class DetailAdapter(
    private val items: List<DetailImageItem>,
    private val onItemClick: (DetailImageItem) -> Unit
) : RecyclerView.Adapter<DetailAdapter.DetailImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detail_image, parent, false)
        return DetailImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailImageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class DetailImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)

        fun bind(item: DetailImageItem) {
            // Загрузка изображения с помощью Glide для оптимизации
            Glide.with(itemView.context)
                .load(item.imagePath)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(imageView)

            nameTextView.text = item.imageName

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}