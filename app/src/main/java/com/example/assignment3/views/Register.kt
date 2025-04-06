package com.example.assignment3.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.assignment3.R
import com.example.assignment3.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth

class Register : AppCompatActivity() {

    lateinit var binding: ActivityRegisterBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the binding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup the firebase auth
        auth = FirebaseAuth.getInstance()


        // Set the on click listener
        binding.register.setOnClickListener {
            // Get the params
            var email: String = binding.emailRegister.text.toString()
            var password1: String = binding.password1Register.text.toString()
            var password2: String = binding.password2Register.text.toString()

            // Check that the params are set
            if (email.isEmpty()) {
                Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show()
            } else if (password1.isEmpty() || password2.isEmpty()) {
                Toast.makeText(this, "Both passwords are required and should be the same", Toast.LENGTH_SHORT).show()
            } else if (password1 != password2) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else if (password1.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            } else {
                // Call the register method
                register(email, password1)
            }
        }

        // Set back to login click listener
        binding.backToLogin.setOnClickListener {
            // Finish this intent
            finish()
        }

    }

    private fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { result ->
                if (result.isSuccessful) {
                    // Go back to login screen
                    finish()

                } else {
                    // Failed to register the user
                    Toast.makeText(this, "Failed to create user", Toast.LENGTH_SHORT).show()
                    Log.w("custom", "signInWithCustomToken:failure", result.exception)
                }
            }
    }
}