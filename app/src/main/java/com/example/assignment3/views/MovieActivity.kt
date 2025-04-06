package com.example.assignment3.views

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.assignment3.R
import com.example.assignment3.databinding.ActivityMovieBinding
import com.example.assignment3.model.MovieModel
import com.example.assignment3.utils.ApiClient
import com.example.assignment3.viewModels.MovieViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class MovieActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMovieBinding
    private lateinit var viewModel: MovieViewModel
    private lateinit var user: FirebaseUser
    private var inFav: Boolean = false
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the movie title
        val movieTitle = intent.getStringExtra("MOVIETITLE")?: "Error getting movie title"
        val movieType = intent.getStringExtra("MOVIETYPE")?: "Error getting movie type"
        val movieReleaseDate = intent.getStringExtra("MOVIERELEASEDATE")?: "Error getting release date"
        val movieRuntime = intent.getStringExtra("MOVIERUNTIME")?: "Error getting movie runtime"
        val movieRating = intent.getStringExtra("MOVIERATING")?: "Error getting movie rating"
        val movieDirector = intent.getStringExtra("MOVIEDIRECTOR")?: "Error getting director"
        var moviePlot = intent.getStringExtra("MOVIEPLOT")?: "Error getting plot"
        val moviePoster = intent.getStringExtra("MOVIEPOSTER")?: ""
        val movieGenre = intent.getStringExtra("MOVIEGENRE")?: "Error getting genre"
        val movieLanguage = intent.getStringExtra("MOVIELANGUAGE")?: "Error getting language"
        val movieImdb = intent.getStringExtra("MOVIEIMDBID")?: ""

        // Observe the movie details
        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]

        // Get the user
        user = intent.getParcelableExtra("USER", FirebaseUser::class.java)!!

        // Determine if this is a favorite movie or not
        db.collection("favMovies")
            .whereEqualTo("imdbID", movieImdb)
            .whereEqualTo("userUUID", user.uid)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                // If we have the movie then its in the favorites
                if (!result.isEmpty) {
                    // Update our UI
                    updateInFav(true)

                    // Update the plot
                    val movie = result.documents[0].toObject(MovieModel::class.java)
                    if (movie != null) {
                        moviePlot = movie.Plot
                    }
                }

                // Update the movie in the view Model
                viewModel.setMovieModel(movieTitle, movieType, movieReleaseDate, movieRuntime, movieRating, movieDirector, moviePlot, moviePoster, movieGenre, movieLanguage)
            }


        // Set the movie in the view Model
        viewModel.setMovieModel(
            movieTitle,
            movieType,
            movieReleaseDate,
            movieRuntime,
            movieRating,
            movieDirector,
            "Fetching....",
            moviePoster,
            movieGenre,
            movieLanguage
        )

        // Fetch the movie
        viewModel.getMovieModel()

        viewModel.getMovieModel().observe(this, Observer { movie ->
            movie?.let {
                if (it.Poster.contains("http")) {
                    ApiClient().loadImageFromUrl(binding.movieLogo, it.Poster)
                }
                binding.movieTitle.text = it.Title
                binding.type.text = "Type: ${it.Type}"
                binding.releaseDate.text = it.Released
                binding.runtime.text = it.Runtime
                binding.movieRating.text = it.rating
                binding.director.text = "Director: ${it.Director}"
                binding.genre.text = "Genre(s): ${it.Genre}"
                binding.languge.text = "Language(s): ${it.Language}"
                binding.plotEdit.setText(it.Plot)
            }
        })

        // Setup add to favorites click
        binding.addToFavs.setOnClickListener {
            if (!this.inFav) {
                // Create the movie
                val movie = MovieModel(
                    Title = movieTitle,
                    Type = movieType,
                    Year = movieReleaseDate,
                    Released = movieReleaseDate,
                    Runtime = movieRuntime,
                    rating = movieRating,
                    Director = movieDirector,
                    Plot = moviePlot,
                    Poster = moviePoster,
                    Genre = movieGenre,
                    Language = movieLanguage,
                    imdbID = movieImdb,
                    userUUID = user.uid
                )

                // Add the movie to the db
                db.collection("favMovies")
                    .add(movie)
                    .addOnSuccessListener { docRef ->
                        // Display a message to the user
                        Toast.makeText(this, "Added movie to favorites", Toast.LENGTH_SHORT).show()

                        // Update UI
                        updateInFav(true)
                    }
                    .addOnFailureListener { err ->
                        Toast.makeText(this, "Failed to add to favorites", Toast.LENGTH_SHORT).show()
                        Log.w("ADDING TO FAVS", "Error adding movie to favs", err)
                    }

            } else {
                // Get the movie to remove
                db.collection("favMovies")
                    .whereEqualTo("imdbID", movieImdb)
                    .whereEqualTo("userUUID", user.uid)
                    .limit(1) // Make sure only one movie is selected
                    .get()
                    .addOnSuccessListener { result ->
                        // Make sure we have a movie selected
                        if (result.isEmpty) {
                            // Let user know of failure
                            Toast.makeText(this, "Failed to remove movie from favorites", Toast.LENGTH_SHORT).show()
                            Log.w("REMOVING MOVIE FROM FAVS", "No movie found when removing movie from favorites")
                            return@addOnSuccessListener
                        }

                        // Delete the movie
                        db.collection("favMovies").document(result.documents[0].id)
                            .delete()
                            .addOnSuccessListener {
                                // Notify user
                                Toast.makeText(this, "Removed movie from favorites", Toast.LENGTH_SHORT).show()

                                // Update UI
                                updateInFav(false)
                            }
                            .addOnFailureListener { err ->
                                // Let user know of failure
                                Toast.makeText(this, "Failed to remove movie from favorites", Toast.LENGTH_SHORT).show()
                                Log.w("REMOVING MOVIE FROM FAVS", "Error removing movie from favs", err)
                            }
                    }
                    .addOnFailureListener { err ->
                        // Let user know of failure
                        Toast.makeText(this, "Failed to remove movie from favorites", Toast.LENGTH_SHORT).show()
                        Log.w("REMOVING MOVIE FROM FAVS", "Error removing movie from favs", err)
                    }
            }
        }

        // Setup back on click
        binding.backButton.setOnClickListener {
            finish()
        }

        // Setup update on click
        binding.update.setOnClickListener {
            val text: String = binding.plotEdit.text.toString()
            db.collection("favMovies")
                .whereEqualTo("imdbID", movieImdb)
                .whereEqualTo("userUUID", user.uid)
                .limit(1)
                .get()
                .addOnSuccessListener { result ->
                    // No movies were found
                    if (result.isEmpty) {
                        // Let user know of failure
                        Toast.makeText(this, "Failed to update movie", Toast.LENGTH_SHORT).show()
                        Log.w("UPDATING MOVIE", "No movie found when updating")
                        return@addOnSuccessListener
                    }

                    // Update the movie
                    result.documents[0].reference.update("plot", text)
                        .addOnSuccessListener {
                            // Notify the user
                            Toast.makeText(this, "Updated movie", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { err ->
                            // Let use know of failure
                            Toast.makeText(this, "Failed to update movie", Toast.LENGTH_SHORT).show()
                            Log.w("UPDATING MOVIE", "Failed to update movie", err)
                        }
                }
                .addOnFailureListener { err ->
                    // Let use know of failure
                    Toast.makeText(this, "Failed to update movie", Toast.LENGTH_SHORT).show()
                    Log.w("UPDATING MOVIE", "Failed to update movie", err)
                }
        }
    }

    fun updateInFav(value: Boolean) {
        // Update the UI elements
        this.inFav = value
        binding.plotEdit.isEnabled = value
        binding.update.isInvisible = !value
        binding.update.isEnabled = value

        // Update text
        if (value) {
            binding.plot.background = ContextCompat.getDrawable(this, R.drawable.rounded_border)
            binding.plotEdit.setPadding(30, 10, 10, 20)
            binding.addToFavs.text = "Remove from favorites"
        } else {
            binding.plot.background = null
            binding.plotEdit.setPadding(0)
            binding.addToFavs.text = "Add to favorites"
        }
    }
}