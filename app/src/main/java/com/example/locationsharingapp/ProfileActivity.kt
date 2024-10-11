package com.example.locationsharingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Perfil"
        setSupportActionBar(toolbar)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.selectedItemId = R.id.action_profile

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_chats -> {
                    Toast.makeText(this, "Chats seleccionados", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        val usernameTextView: TextView = findViewById(R.id.user_name)
        val emailTextView: TextView = findViewById(R.id.user_email)
        val userInfoTextView: TextView = findViewById(R.id.user_info)

        val username = "Eduar Avendaño"
        val email = "xavieravendano9@gmail.com"
        val userInfo = "Ingeniero de software en proyectos de ubicación"

        usernameTextView.text = "$username"
        emailTextView.text = " - $email"
        userInfoTextView.text = " - $userInfo"

    }
}
