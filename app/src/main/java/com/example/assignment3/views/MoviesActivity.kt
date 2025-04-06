package com.example.assignment3.views

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
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
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken


class MoviesActivity : AppCompatActivity(), MovieClickListener, RequestCallback {

    private lateinit var binding: ActivityMoviesBinding
    private lateinit var viewModel: MovieViewModel
    private lateinit var recyclerView: RecyclerView

    private lateinit var myAdapter: Adapter

    private lateinit var moviesList: ArrayList<MovieModel>
    private var addingMovies: Boolean = false

    private lateinit var user: FirebaseUser

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
        ApiClient().searchMovies("the maze", this)

        // Get the user
        user = intent.getParcelableExtra("USER", FirebaseUser::class.java)!!

        // Add listeners
        binding.search.setOnClickListener {
            val searchText: String = binding.searchInput.text.toString()
            ApiClient().searchMovies(searchText, this)
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

    override fun onAPIReturn(json: String) {
        // Get the json version of the string
        val gson = Gson()
        val jsonObject = gson.fromJson(json, JsonObject::class.java)

        // Check if we didn't get any data and display a
        // toast message if that's true
        if (jsonObject["Response"].asString == "False") {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, "Unable to complete search", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Get the searched dict value
        val searchJson = gson.toJson(jsonObject["Search"])

        // Create a movie arraylist type
        val movieListType = object : TypeToken<ArrayList<MovieModel>>() {}.type

        // Get the movies
        val movies: ArrayList<MovieModel> = Gson().fromJson(searchJson, movieListType)

        // Get the movie details and update the display as they come in
        var count = 0
        for (movie in movies) {
            // Get the movie details
            ApiClient().getMovieDetails(movie.imdbID, { movieDetails: String ->
                // Make the string into a json
                val movieJsonObject = gson.fromJson(movieDetails, JsonObject::class.java)

                // Check if request was successful
                if (jsonObject["Response"].asString == "False") {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this, "Unable to complete search", Toast.LENGTH_SHORT).show()
                    }
                    return@getMovieDetails
                }

                // Create a type of movie within a type token
                val movieType = object : TypeToken<MovieModel>() {}.type

                // Get the movie
                val movieObject: MovieModel = gson.fromJson(gson.toJson(movieJsonObject), movieType)

                // Extract the first rating, if available
                val ratingsArray: JsonArray? = movieJsonObject.getAsJsonArray("Ratings")
                movieObject.rating = if (ratingsArray != null && ratingsArray.size() > 0) {
                    ratingsArray[0].asJsonObject.get("Value")?.asString ?: ""
                } else {
                    ""
                }

                // If we are giving the last movie send the -1 response count
                Handler(Looper.getMainLooper()).post {
                    if (movies.size - 1 > count) {
                        this.addMovie(movieObject, count)
                    } else {
                        this.addMovie(movieObject, -1)
                    }

                    // If count is not placed here it will increment before
                    // the value is used resulting in no movies being added
                    // to the screen
                    count++
                }
            })
        }
    }

    private fun addMovie(movie: MovieModel, count: Int) {
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