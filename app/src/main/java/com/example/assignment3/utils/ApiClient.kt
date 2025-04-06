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

    fun searchMovies(searchText: String, context: RequestCallback) {
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

                    // Return the json
                    context.onAPIReturn(string)
                }
            }
        })
    }

    fun getMovieDetails(imdbId: String, callback: (json: String) -> Unit = {}) {
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

                    // Send the string response back to the calling func
                    callback(string)
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