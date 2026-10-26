package com.example.proyecto_atackdeck_android

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_atackdeck_android.databinding.ActivityUserDeckBinding
import com.example.proyecto_atackdeck_android.BottomNavHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_atackdeck_android.repository.PokemonRepository


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

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        binding.btnlogout.setOnClickListener {

            auth.signOut()


            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this@UserDeckActivity, EditProfileActivity::class.java)
            startActivity(intent)
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
        loadUserTeams()
        BottomNavHelper.setupBottomNav(
            activity = this,
            bottomNavigationView = binding.includedBottomNav.root,
            currentItemId = R.id.nav_user_deck
        )
    }

    private fun loadUserTeams() {
        lifecycleScope.launch {
            val repository = PokemonRepository()
            val misEquipos = repository.getUserTeams()

            val adapter = TeamAdapter(
                teamList = misEquipos,
                onPokemonClick = { pokemonId ->
                    val intent = Intent(this@UserDeckActivity, PokemonDetailActivity::class.java)
                    intent.putExtra(PokemonDetailActivity.EXTRA_POKEMON_ID, pokemonId)
                    startActivity(intent)
                },
                onEditClick = { equipoAEditar ->

                    val intent = Intent(this@UserDeckActivity, EditUserTeamActivity::class.java)
                    intent.putExtra("EXTRA_TEAM_ID", equipoAEditar.teamId)
                    startActivity(intent)
                },
                onDeleteClick = { equipoABorrar ->
                    lifecycleScope.launch {
                        val exito = repository.deleteUserTeam(equipoABorrar.teamId)
                        if (exito) {
                            Toast.makeText(this@UserDeckActivity, "Equipo eliminado", Toast.LENGTH_SHORT).show()
                            loadUserTeams() // Recargamos la lista
                        } else {
                            Toast.makeText(this@UserDeckActivity, "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onPublishClick = { equipoAPublicar ->
                    // Ejecuta la función para mostrar la ventana de publicación
                    mostrarDialogoPublicar(equipoAPublicar, repository)
                }
            )

            binding.rvUserTeams.layoutManager = LinearLayoutManager(this@UserDeckActivity)
            binding.rvUserTeams.adapter = adapter
        }
    }

    private fun mostrarDialogoPublicar(equipo: com.example.proyecto_atackdeck_android.model.UserTeam, repository: PokemonRepository) {
        val input = android.widget.EditText(this)
        input.hint = "Escribe una breve descripción o estrategia..."

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Publicar Equipo")
            .setMessage("Comparte tu equipo '${equipo.teamName}' con la comunidad.")
            .setView(input)
            .setPositiveButton("Publicar") { _, _ ->
                val descripcion = input.text.toString().trim()

                lifecycleScope.launch {

                    val exito = repository.uploadCommunityTeam(
                        teamName = equipo.teamName,
                        description = descripcion,
                        pokemonsList = equipo.pokemons
                    )

                    if (exito) {
                        Toast.makeText(this@UserDeckActivity, "¡Equipo subido a la comunidad!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@UserDeckActivity, "Error al publicar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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