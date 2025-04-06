package com.example.assignment3.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment3.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class Login : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup the auth
        auth = FirebaseAuth.getInstance()

        // Setup button clicks
        binding.loginButton.setOnClickListener {
            // Check if the email and password are set
            if (binding.email.text.toString().isEmpty()) {
                Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show()
            } else if (binding.password.text.toString().isEmpty()) {
                Toast.makeText(this, "A password is required", Toast.LENGTH_SHORT).show()
            } else {
                // Call the sign in method
                signIn(binding.email.text.toString(), binding.password.text.toString())
            }
        }

        binding.registerButton.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { result ->
                if (result.isSuccessful) {
                    // Get the user
                    val user = auth.currentUser

                    // Setup the intent
                    val intent: Intent = Intent(this, MoviesActivity::class.java).apply {
                        putExtra("USER", user)
                    }

                    // Start the intent
                    startActivity(intent)

                } else {
                    // Failed to log in
                    Toast.makeText(this, "Failed to login", Toast.LENGTH_SHORT).show()

                }
            }
    }
}