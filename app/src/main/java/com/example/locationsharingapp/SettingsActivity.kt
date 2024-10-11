package com.example.locationsharingapp

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Ajustes"
        setSupportActionBar(toolbar)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.selectedItemId = R.id.action_settings

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

        val listView: ListView = findViewById(R.id.settings_list)

        // Opciones del menú
        val options = arrayOf("Agregar biometría", "Cerrar sesión")

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
        listView.adapter = adapter

        // Listener para manejar los clics en los elementos del menú
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    val intent = Intent(this, AddBiometryActivity::class.java)
                    startActivity(intent)
                    //Toast.makeText(this, "Biometría", Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    //Toast.makeText(this, "Login", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Opción no válida", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
