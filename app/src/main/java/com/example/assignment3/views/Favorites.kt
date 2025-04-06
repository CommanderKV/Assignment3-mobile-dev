package com.example.assignment3.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assignment3.databinding.ActivityFavoritesBinding
import com.example.assignment3.model.MovieModel
import com.example.assignment3.utils.Adapter
import com.example.assignment3.utils.MovieClickListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class Favorites : AppCompatActivity(), MovieClickListener {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var user: FirebaseUser
    private val db = FirebaseFirestore.getInstance()
    private lateinit var movies: ArrayList<MovieModel>

    // Lambda functions
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Update the moves after the intent finishes
        updateMovies()
    }
    val updateAdapter = { adapter: Adapter ->
        binding.favMoviesList.adapter = adapter
        adapter.clickListener = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup binding
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the user from the calling activity
        user = intent.getParcelableExtra("USER", FirebaseUser::class.java)!!
        Log.w("USER", "user: ${user.email}")

        // Setup the layout manager
        binding.favMoviesList.layoutManager = LinearLayoutManager(this)

        // Setup the arraylist of movies
        movies = ArrayList()

        // Update the movies list
        this.updateMovies()

        // Deal with nav button
        binding.navMovies.setOnClickListener {
            // Finish this intent
            finish()
        }
    }

    private fun updateMovies() {
        // Get the movies from favMovies
        db.collection("favMovies")
            .whereEqualTo("userUUID", user.uid)
            .get()
            .addOnSuccessListener { result ->
                // Clear the current list of movies and the display
                this.movies.clear()
                updateAdapter(Adapter(this.movies))

                // If the result is empty tell user
                if (result.isEmpty) {
                    Toast.makeText(this, "No favorited movies were found", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                // Add each movie to the list
                for (document in result) {
                    // Get the movie and add it
                    val movie = document.toObject(MovieModel::class.java)
                    this.movies.add(movie)

                    // Update the adapter
                    updateAdapter(Adapter(this.movies))
                }
            }
            .addOnFailureListener { exception ->
                Log.w("UPDATE MOVIES", "Error getting movies", exception)
            }
    }

    override fun onClick(v: View, pos: Int) {
        val movie: MovieModel = this.movies[pos]

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

            putExtra("INFAV", true)
            putExtra("USER", user)
        }
        launcher.launch(intent)
    }

}