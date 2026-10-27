package com.example.proyecto_atackdeck_android.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_atackdeck_android.util.BottomNavHelper
import com.example.proyecto_atackdeck_android.R
import com.example.proyecto_atackdeck_android.adapter.RankingAdapter
import com.example.proyecto_atackdeck_android.adapter.TeamAdapter
import com.example.proyecto_atackdeck_android.databinding.ActivityMainBinding
import com.example.proyecto_atackdeck_android.repository.PokemonRepository
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val repository = PokemonRepository() // Conexión a Firebase
    private lateinit var rankingAdapter: RankingAdapter

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            // No hay sesión activa, redirigir a LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return // Detiene la ejecución del resto del onCreate
        }

        val nombreUsuario = currentUser.email?.substringBefore("@") ?: "Usuario"
        binding.tvWelcome.text = "Welcome "+ nombreUsuario


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }





        // 1. Configuramos el LayoutManager del RecyclerView para que sea una lista vertical
        binding.rvMetaPokemons.layoutManager = LinearLayoutManager(this)

        // 2. Cargamos los datos desde Firebase
        loadRankingFromFirebase()
        loadMetaTeams()



        binding.btnViewMoreRanking.setOnClickListener {
            val intent = Intent(this@MainActivity, MetaPokemonActivity::class.java)
            startActivity(intent)
        }
        binding.btnViewMoreTeams.setOnClickListener {
            val intent = Intent(this@MainActivity, MetaTeamsActivity::class.java)
            startActivity(intent)
        }
        binding.btnCommunityTeams.setOnClickListener {
            val intent = Intent(this@MainActivity, CommunityTeamsActivity::class.java)
            startActivity(intent)
        }
        binding.btnHelp.setOnClickListener {
            val intent = Intent(this@MainActivity, HelpActivity::class.java)
            startActivity(intent)
        }




    }
    override fun onResume() {
        super.onResume()
        cargarDatosDelPerfil()
        BottomNavHelper.setupBottomNav(
            activity = this,
            bottomNavigationView = binding.includedBottomNav.root,
            currentItemId = R.id.nav_home
        )
    }

    private fun loadMetaTeams() {
        lifecycleScope.launch {
            val repository = PokemonRepository()
            val teamsList = repository.getMetaTeams(limite = 5) // Trae los primeros 5 equipos

            val adapter = TeamAdapter(
                teamList = teamsList,
                onPokemonClick = { pokemonId ->

                    val intent = Intent(this@MainActivity, PokemonDetailActivity::class.java)
                    intent.putExtra(PokemonDetailActivity.EXTRA_POKEMON_ID, pokemonId)
                    startActivity(intent)
                },
                onSaveClick = { metaTeamSeleccionado ->
                    // Guardar el equipo meta en la cuenta del usuario actual
                    lifecycleScope.launch {
                        val exito = repository.saveMetaTeamToUser(metaTeamSeleccionado)
                        if (exito) {
                            Toast.makeText(
                                this@MainActivity,
                                "¡Equipo guardado en tu perfil!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Error al guardar el equipo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )

            // Asegúrate de tener un RecyclerView en tu XML con este ID o cámbialo al tuyo
            binding.rvMetaTeams.layoutManager = LinearLayoutManager(this@MainActivity)
            binding.rvMetaTeams.adapter = adapter
        }
    }

    private fun cargarDatosDelPerfil() {
        val currentUser = auth.currentUser ?: return
        val defaultName = currentUser.email?.substringBefore("@") ?: "Usuario"
        binding.tvWelcome.text = "Welcome $defaultName"
        val userRef = db.collection("users_profiles").document(currentUser.uid)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {

                val nombreGuardado = document.getString("userName")


                if (!nombreGuardado.isNullOrEmpty()) {
                    binding.tvWelcome.text = "Welcome $nombreGuardado"
                } else {
                    binding.tvWelcome.text = currentUser.email?.substringBefore("@") ?: "Welcome $currentUser"
                }


            }
        }.addOnFailureListener {
            binding.tvWelcome.text = currentUser.email?.substringBefore("@") ?: "Welcome $currentUser"
        }
    }

    private fun loadRankingFromFirebase() {
        lifecycleScope.launch {
            // Obtenemos la lista desde Firestore
            val listaPokemons = repository.getSavedPokemons(limite = 4)

            // Instanciamos el Adapter pasándole la lista
            rankingAdapter = RankingAdapter(listaPokemons) { pokemonSeleccionado ->
                // Este código se ejecuta cuando el usuario toca un elemento de la lista
                val intent = Intent(this@MainActivity, PokemonDetailActivity::class.java)
                intent.putExtra(
                    PokemonDetailActivity.EXTRA_POKEMON_ID,
                    pokemonSeleccionado.pokemonId
                )
                startActivity(intent)
            }

            // Conectamos el Adapter al RecyclerView
            binding.rvMetaPokemons.adapter = rankingAdapter
        }
    }
}