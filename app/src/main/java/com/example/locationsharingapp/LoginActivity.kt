package com.example.locationsharingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.locationsharingapp.Const.HostConfig
import com.example.locationsharingapp.services.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var httpClient: HttpClient = HttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText: EditText = findViewById(R.id.email)
        val passwordEditText: EditText = findViewById(R.id.password)
        val loginButton: Button = findViewById(R.id.login_button)
        val biometricButton: Button = findViewById(R.id.biometric_button)

        executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Si la autenticación biométrica es exitosa, pasa a MainActivity
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish() // Cierra la actividad de login
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Autenticación fallida", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Iniciar sesión")
            .setSubtitle("Usa tu huella o reconocimiento facial para iniciar sesión")
            .setNegativeButtonText("Cancelar")
            .build()

        // Configura el botón de inicio de sesión
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Usar Coroutine para la solicitud de red en un hilo de fondo
                CoroutineScope(Dispatchers.IO).launch {
                    val response = httpClient.doPostRequest(
                        HostConfig.HOST + "/login",
                        """{"email": "$email", "password": "$password"}"""
                    )

                    withContext(Dispatchers.Main) {
                        if (response != null) {
                            try {
                                // Convertir la respuesta a JSON
                                val jsonResponse = JSONObject(response)

                                if (jsonResponse.has("id")) {
                                    val userId = jsonResponse.getString("id")

                                    // Lanzar la siguiente actividad con los datos del usuario
                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    intent.putExtra("user_id", userId)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    // Mostrar un mensaje si no se encuentra el campo "id"
                                    Toast.makeText(this@LoginActivity, "Inicio de sesión incorrecto", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                // Manejar excepciones si ocurre un error al analizar la respuesta
                                e.printStackTrace()
                                Toast.makeText(this@LoginActivity, "Error al analizar la respuesta", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Mostrar un mensaje si no hay respuesta
                            Toast.makeText(this@LoginActivity, "Error de conexión o respuesta nula", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                // Si el correo o la contraseña están vacíos
                Toast.makeText(this, "Por favor ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
            }
        }

        // Configura el botón de biometría
        biometricButton.setOnClickListener {
            checkBiometricSupport()
        }
    }

    private fun checkBiometricSupport() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this, "Este dispositivo no tiene hardware biométrico", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, "Hardware biométrico no disponible", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this, "No hay biometría configurada en este dispositivo", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
