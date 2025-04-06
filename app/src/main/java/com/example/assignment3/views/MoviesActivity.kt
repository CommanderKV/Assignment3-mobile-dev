package com.example.assignment3.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment3.utils.MovieClickListener
import com.example.assignment3.databinding.ActivityMoviesBinding
import com.example.assignment3.model.MovieModel
import com.example.assignment3.utils.Adapter
import com.example.assignment3.utils.ApiClient
import com.example.assignment3.utils.RequestCallback
import com.example.assignment3.viewModels.MovieViewModel
import com.google.firebase.auth.FirebaseUser


class MoviesActivity : AppCompatActivity(), MovieClickListener, RequestCallback {

    lateinit var binding: ActivityMoviesBinding
    lateinit var viewModel: MovieViewModel
    lateinit var recyclerView: RecyclerView

    lateinit var myAdapter: Adapter

    lateinit var moviesList: ArrayList<MovieModel>
    var addingMovies: Boolean = false

    lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the bindings
        binding = ActivityMoviesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the viewModel
        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]

        // Get the recycler view
        recyclerView = binding.moviesList

        // Create a linear view manager
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Make a call to the api
        ApiClient().searchMovies("the maze", this, this)

        // Get the user
        user = intent.getParcelableExtra("USER", FirebaseUser::class.java)!!

        // Add listeners
        binding.search.setOnClickListener {
            val searchText: String = binding.searchInput.text.toString()
            ApiClient().searchMovies(searchText, this, this)
        }

        // Build temp movies list
        moviesList = arrayListOf(MovieModel())


        // Deal with nav button
        binding.navFavs.setOnClickListener {
            // Send the user to the favorites page
            val intent = Intent(this, Favorites::class.java).apply {
                putExtra("USER", user)
            }
            startActivity(intent)
        }
    }

    override fun onClick(v: View, pos: Int) {
        // Get the selected movie
        val movie: MovieModel = moviesList[pos]
        val intent: Intent = Intent(this, MovieActivity::class.java).apply {
            putExtra("MOVIETITLE", movie.Title)
            putExtra("MOVIETYPE", movie.Type)
            putExtra("MOVIERELEASEDATE", movie.Released)
            putExtra("MOVIERUNTIME", movie.Runtime)
            putExtra("MOVIERATING", movie.rating)
            putExtra("MOVIEDIRECTOR", movie.Director)
            putExtra("MOVIEPLOT", movie.Plot)
            putExtra("MOVIEPOSTER", movie.Poster)
            putExtra("MOVIEGENRE", movie.Genre)
            putExtra("MOVIELANGUAGE", movie.Language)

            putExtra("MOVIEIMDBID", movie.imdbID)
            putExtra("USER", user)
        }
        startActivity(intent)
    }

    override fun onAPIReturn(movie: MovieModel, count: Int) {
        println("${addingMovies} -- ${count}")
        // Check if we are done sending movies to the recycler view
        if (count == -1) {
            moviesList.add(movie)
            addingMovies = false

        // Check if we are sending new data to the recycler view
        } else if (count == 0) {
            addingMovies = true
            moviesList = arrayListOf(movie)

        // Add the movies if we are allowed to
        } else if (addingMovies) {
            moviesList.add(movie)
        }

        // Reset the recycler and adapter
        myAdapter = Adapter(moviesList)
        recyclerView.adapter = myAdapter
        myAdapter.clickListener = this
    }
}