package com.example.proyecto_atackdeck_android.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_atackdeck_android.adapter.AvatarAdapter
import com.example.proyecto_atackdeck_android.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    // Lista de nombres de archivos que tengas en tu carpeta res/drawable
    private val listaAvatares = listOf(
        "avatar_1", "avatar_2", "avatar_3", "avatar_4",
        "avatar_5", "avatar_6", "avatar_7", "avatar_8",
        "avatar_9", "avatar_10", "avatar_11", "avatar_12",
        "avatar_14","avatar_15","avatar_16","avatar_17"
    )

    private var avatarSeleccionadoActual = "avatar_1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()





        val adapter = AvatarAdapter(listaAvatares) { avatarName ->

            avatarSeleccionadoActual = avatarName
            actualizarImagenPrincipal(avatarName)
        }
        binding.rvAvatars.adapter = adapter
        binding.btnVolver.setOnClickListener { finish() }

        cargarDatosDeUsuario()

        // 4. Guardar Cambios
        binding.btnSaveChanges.setOnClickListener {
            guardarCambiosEnFirebase()
        }
    }

    private fun cargarDatosDeUsuario() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("users_profiles").document(uid)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val userName = document.getString("userName") ?: ""
                val nintendoId = document.getString("nintendoId") ?: ""
                val avatarGuardado = document.getString("avatarName") ?: listaAvatares[0]


                binding.etUserName.setText(userName)
                binding.etNintendoId.setText(nintendoId)


                avatarSeleccionadoActual = avatarGuardado
                actualizarImagenPrincipal(avatarGuardado)
            }
        }
    }

    private fun actualizarImagenPrincipal(nombreAvatar: String) {
        val resourceId = resources.getIdentifier(nombreAvatar, "drawable", packageName)
        if (resourceId != 0) {
            binding.imgSelectedAvatar.setImageResource(resourceId)
        }
    }

    private fun guardarCambiosEnFirebase() {
        val uid = auth.currentUser?.uid ?: return

        val newUserName = binding.etUserName.text.toString().trim()
        val newNintendoId = binding.etNintendoId.text.toString().trim()

        if (newUserName.isEmpty()) {
            Toast.makeText(this, "El nombre de usuario no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        val datosAGuardar = hashMapOf(
            "userName" to newUserName,
            "nintendoId" to newNintendoId,
            "avatarName" to avatarSeleccionadoActual
        )

        // Set con merge (o update) para no borrar otros datos si existieran
        db.collection("users_profiles").document(uid)
            .set(datosAGuardar)
            .addOnSuccessListener {
                Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                finish() // Cierra esta pantalla y regresa a UserDeckActivity
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}