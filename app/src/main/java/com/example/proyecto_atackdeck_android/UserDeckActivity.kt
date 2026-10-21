package com.example.proyecto_atackdeck_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_atackdeck_android.databinding.ActivityUserDeckBinding
import com.example.proyecto_atackdeck_android.BottomNavHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class UserDeckActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDeckBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserDeckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Inicializar Firebase Auth y verificar sesión
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            // No hay sesión activa, redirigir a LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return // Detiene la ejecución del resto del onCreate
        }

        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this@UserDeckActivity, EditProfileActivity::class.java)
            startActivity(intent)
        }
        val userRef = db.collection("users_profiles").document(currentUser.uid)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists() && document.contains("avatarName")) {
                // Si el usuario ya tiene un avatar guardado, lo cargamos
                val avatarGuardado = document.getString("avatarName") ?: "avatar_entrenador"
                cargarAvatarEnPantalla(avatarGuardado)
            } else {
                // Si es la primera vez que entra, le ponemos uno por defecto
                cargarAvatarEnPantalla("avatar_entrenador") // Asegúrate de tener una imagen llamada así en drawable
            }
        }.addOnFailureListener {
            // Si falla el internet, cargamos el por defecto
            cargarAvatarEnPantalla("avatar_entrenador")
        }


        BottomNavHelper.setupBottomNav(
            activity = this,
            bottomNavigationView = binding.includedBottomNav.root,
            currentItemId = R.id.nav_user_deck
        )
    }
    override fun onResume() {
        super.onResume()
        cargarDatosDelPerfil()
    }

    private fun cargarDatosDelPerfil() {
        val currentUser = auth.currentUser ?: return
        val userRef = db.collection("users_profiles").document(currentUser.uid)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {

                val avatarGuardado = document.getString("avatarName") ?: "avatar_entrenador"
                val nombreGuardado = document.getString("userName")
                val nintendoIdGuardado = document.getString("nintendoId")


                cargarAvatarEnPantalla(avatarGuardado)


                if (!nombreGuardado.isNullOrEmpty()) {
                    binding.tvUserName.text = nombreGuardado
                } else {
                    binding.tvUserName.text = currentUser.email?.substringBefore("@") ?: "Usuario"
                }


                if (!nintendoIdGuardado.isNullOrEmpty()) {
                    binding.tvNintendoId.text = "NintendoID: $nintendoIdGuardado"
                } else {
                    binding.tvNintendoId.text = "NintendoID: ${currentUser.uid.take(10)}"
                }
            } else {

                cargarAvatarEnPantalla("avatar_entrenador")
                binding.tvUserName.text = currentUser.email?.substringBefore("@") ?: "Usuario"
                binding.tvNintendoId.text = "NintendoID: ${currentUser.uid.take(10)}"
            }
        }.addOnFailureListener {

            cargarAvatarEnPantalla("avatar_entrenador")
            binding.tvUserName.text = currentUser.email?.substringBefore("@") ?: "Usuario"
        }
    }


    private fun cargarAvatarEnPantalla(nombreAvatar: String) {

        val resourceId = resources.getIdentifier(nombreAvatar, "drawable", packageName)

        // 2. Verificamos que la imagen exista (si no existe, resourceId será 0)
        if (resourceId != 0) {
            binding.imgUserProfile.setImageResource(resourceId)
        } else {

            binding.imgUserProfile.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }
}