package com.example.assignment3.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.Toast
import com.example.assignment3.model.MovieModel
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

class ApiClient {
    private val client = OkHttpClient()
    private val URL = "https://www.omdbapi.com/?apikey=cd9b5acb"

    fun searchMovies(searchText: String, context: Context, callback: RequestCallback) {
        val request = Request.Builder()
            .url("${URL}&s=${searchText}")
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    // Get the string version of the response
                    val string = response.body?.string() ?: ""
                    println(string)

                    // Get the handler so we can make calls to the main loop
                    val mainHandler = Handler(Looper.getMainLooper())

                    // Get the json version of the string
                    val gson = Gson()
                    val jsonObject = gson.fromJson(string, JsonObject::class.java)

                    // Check if we didn't get any data and display a
                    // toast message if that's true
                    if (jsonObject["Response"].asString == "False") {
                        mainHandler.post {
                            Toast.makeText(context, "Unable to complete search", Toast.LENGTH_SHORT)
                                .show()
                        }
                        return
                    }

                    // Get the searched dict value
                    val searchJson = gson.toJson(jsonObject["Search"])

                    // Get the type we want returned
                    val type = object : TypeToken<ArrayList<MovieModel>>() {}.type

                    val movies: ArrayList<MovieModel> = Gson()
                        .fromJson<ArrayList<MovieModel>>(searchJson, type)

                    // Get the movie details and send back one movie at a time
                    var count = 0
                    for (movie in movies) {
                        getMovieDetails(movie.imdbID, context, { movieDetails: MovieModel ->
                            mainHandler.post {
                                // If we are giving the last movie send the -1 response count
                                if (movies.size - 1 > count) {
                                    callback.onAPIReturn(movieDetails, count)
                                } else {
                                    callback.onAPIReturn(movieDetails, -1)
                                }

                                // If count is not placed here it will increment before
                                // the value is used resulting in no movies being added
                                // to the screen
                                count++
                            }
                        })
                    }
                }
            }
        })
    }

    fun getMovieDetails(imdbId: String, context: Context, callback: (movie: MovieModel) -> Unit = {}) {
        // Set the request to get the movie by title
        val request = Request.Builder()
            .url("${URL}&i=${imdbId}")
            .build()

        // Begin the HTTP call
        client.newCall(request).enqueue(object: Callback {
            // On error print it out
            override fun onFailure(call: Call, e: java.io.IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    // Get the string version of the response
                    val string = response.body?.string() ?: ""

                    // Get the handler so we can make calls to the main loop
                    val mainHandler = Handler(Looper.getMainLooper())

                    // Get the json version of the string
                    val gson = Gson()
                    val jsonObject = gson.fromJson(string, JsonObject::class.java)
                    if (jsonObject["Response"].asString == "False") {
                        mainHandler.post {
                            Toast.makeText(context, "Unable to complete search", Toast.LENGTH_SHORT)
                                .show()
                        }
                        return
                    }

                    // Get the movie
                    val type = object : TypeToken<MovieModel>() {}.type
                    val json = gson.toJson(jsonObject)
                    val movie: MovieModel = gson.fromJson(json, type)

                    // Extract the first rating, if available
                    val ratingsArray: JsonArray? = jsonObject.getAsJsonArray("Ratings")
                    movie.rating = if (ratingsArray != null && ratingsArray.size() > 0) {
                        ratingsArray[0].asJsonObject.get("Value")?.asString ?: ""
                    } else {
                        ""
                    }

                    // Return the movie
                    callback(movie)
                }
            }
        })
    }

    fun loadImageFromUrl(imageView: ImageView, url: String) {
        // If the poster link isn't a link return
        if (!url.contains("http")) {
            return
        }

        // Setup get request
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        // Run the HTTP request
        client.newCall(request).enqueue(object : Callback {
            // If there is an error print it out
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // If the request wasn't successful then return
                if (!response.isSuccessful) return

                // Get the image into a byte stream
                response.body?.byteStream()?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    // Update ImageView on main thread
                    Handler(Looper.getMainLooper()).post {
                        imageView.setImageBitmap(bitmap)
                    }
                }
            }
        })
    }
}