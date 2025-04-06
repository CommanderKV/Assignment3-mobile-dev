package com.example.assignment3.views

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment3.R
import com.example.assignment3.utils.MovieClickListener


class MyViewHolder(itemView: View, clickListener: MovieClickListener) : RecyclerView.ViewHolder(itemView) {

    var clickListener: MovieClickListener

    var image: ImageView = itemView.findViewById<ImageView>(R.id.logo)
    var title: TextView = itemView.findViewById<TextView>(R.id.title_txt)
    var date: TextView = itemView.findViewById<TextView>(R.id.date)
    var rating: TextView = itemView.findViewById<TextView>(R.id.rating)

    init {
        this.clickListener = clickListener
    }
}