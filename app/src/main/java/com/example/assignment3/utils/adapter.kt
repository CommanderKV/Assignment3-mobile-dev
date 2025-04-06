package com.example.assignment3.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment3.R
import com.example.assignment3.model.MovieModel
import com.example.assignment3.views.MyViewHolder

class Adapter(private val movies: ArrayList<MovieModel>) : RecyclerView.Adapter<MyViewHolder>() {
    lateinit var clickListener: MovieClickListener

    // Creates new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflates the movie_layout design
        // that is to hold the list movies
        val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_layout, parent, false)

        return MyViewHolder(view, this.clickListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Get the current movie
        val movie = movies[position]

        // Set the details of the movie layout
        if (movie.Poster.contains("http")) {
            ApiClient().loadImageFromUrl(holder.image, movie.Poster)
        }
        holder.title.text = movie.Title
        holder.date.text = movie.Year
        holder.rating.text = movie.rating

        holder.itemView.setOnClickListener {
            clickListener.onClick(holder.itemView, position)
        }
    }

    override fun getItemCount(): Int {
        // Return the size of the movies
        return movies.size
    }
}