package com.example.assignment3.utils

import com.example.assignment3.model.MovieModel

interface RequestCallback {
    fun onAPIReturn(json: String)
}