package com.example.assignment3.model

data class MovieModel (
    var Title: String = "",
    var Year: String = "",
    var imdbID: String = "",
    var Type: String = "",
    var Poster: String = "",

    // These are in a specified search
    var rating: String = "",
    var Released: String = "",
    var Runtime: String = "",
    var Director: String = "",
    var Plot: String = "",
    var Genre: String = "",
    var Language: String = "",
    var userUUID: String = ""
)