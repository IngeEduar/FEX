package com.example.locationsharingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class AddBiometryActivity : AppCompatActivity() {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_biometry)

        executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                saveBiometry()
                Toast.makeText(applicationContext, "Biometría guardada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Autenticación fallida", Toast.LENGTH_SHORT).show()

                finish()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Agregar biometría")
            .setSubtitle("Usa tu huella o reconocimiento facial para agregar")
            .setDescription("Se utilizará para autenticación en futuros inicios de sesión")
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        checkBiometricSupport()
    }

    private fun checkBiometricSupport() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this, "Este dispositivo no tiene hardware biométrico", Toast.LENGTH_SHORT).show()
                finish()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, "Hardware biométrico no disponible", Toast.LENGTH_SHORT).show()
                finish()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this, "No hay biometría configurada en este dispositivo", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun saveBiometry() {
        val sharedPref = getSharedPreferences("BiometryData", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("biometry_saved", true)
            apply()
        }
    }
}
