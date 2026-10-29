package com.example.proyecto_atackdeck_android.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_atackdeck_android.databinding.ActivityLoginBinding
import androidx.activity.result.contract.ActivityResultContracts
import com.example.proyecto_atackdeck_android.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Cuenta de Google seleccionada con éxito, ahora validamos con Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, getString(R.string.error_google, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {

            irAlPerfil()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)

            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Botón Iniciar Sesión
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            irAlPerfil()
                        } else {
                            Toast.makeText(this, getString(R.string.error_exception, task.exception?.message), Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, R.string.msg_fields_empty, Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Registrar (Crea la cuenta y loguea automáticamente)
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, R.string.msg_profile_update_success, Toast.LENGTH_SHORT).show()
                            irAlPerfil()
                        } else {
                            Toast.makeText(this, getString(R.string.error_exception, task.exception?.message), Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, R.string.msg_fields_empty, Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnGoogleLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    irAlPerfil()
                } else {
                    Toast.makeText(this, R.string.error_firebase_auth, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun irAlPerfil() {
        val intent = Intent(this, UserDeckActivity::class.java)
        startActivity(intent)
        finish() // Cierra el login para que no puedan volver con el botón "Atrás"
    }
}