package com.example.locationsharingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.locationsharingapp.Const.HostConfig
import com.example.locationsharingapp.services.HttpClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import org.json.JSONObject

class ProfileActivity : AppCompatActivity() {

    private lateinit var userId: String
    private val httpClient = HttpClient()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        userId = intent.getStringExtra("user_id") ?: "2"

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Perfil"
        setSupportActionBar(toolbar)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.selectedItemId = R.id.action_profile

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("user_id", userId)
                    startActivity(intent)
                    true
                }
                R.id.action_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("user_id", userId)
                    startActivity(intent)
                    true
                }
                R.id.action_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    intent.putExtra("user_id", userId)
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

        // Realizamos la solicitud GET para obtener la información del usuario
        CoroutineScope(Dispatchers.IO).launch {
            val response = httpClient.doGetRequest(HostConfig.HOST + "/users/$userId")

            withContext(Dispatchers.Main) {
                if (response != null) {
                    try {
                        // Convertir la respuesta a JSON
                        val jsonResponse = JSONObject(response)
                        if (jsonResponse.has("username")) {
                            val username = jsonResponse.getString("username")
                            val email = jsonResponse.getString("email")
                            val userInfo = jsonResponse.getString("hobbies")

                            val usernameTextView: TextView = findViewById(R.id.user_name)
                            val emailTextView: TextView = findViewById(R.id.user_email)
                            val userInfoTextView: TextView = findViewById(R.id.user_info)

                            usernameTextView.text = username
                            emailTextView.text = " - $email"
                            userInfoTextView.text = " - $userInfo"
                        } else {
                            Toast.makeText(this@ProfileActivity, "No se encontraron los datos del usuario", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@ProfileActivity, "Error al analizar la respuesta", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Error de conexión o respuesta nula", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
