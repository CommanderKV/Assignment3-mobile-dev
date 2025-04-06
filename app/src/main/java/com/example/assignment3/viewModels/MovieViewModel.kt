package com.example.assignment3.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.assignment3.model.MovieModel

class MovieViewModel() : ViewModel() {

    private val movieData = MutableLiveData<MovieModel>()

    fun getMovieModel(): LiveData<MovieModel> {
        return this.movieData
    }

    fun setMovieModel(title: String, type: String, releaseDate: String, runtime: String, rating: String, director: String, plot: String, poster: String, genre: String, language: String) {
        val movie = MovieModel(
            Title=title,
            Released=releaseDate,
            Year=releaseDate,
            Runtime=runtime,
            rating=rating,
            Director=director,
            Plot=plot,
            Poster=poster,
            Genre = genre,
            Language = language,
            imdbID="",
            Type=type
        )
        movieData.value = movie
    }
}