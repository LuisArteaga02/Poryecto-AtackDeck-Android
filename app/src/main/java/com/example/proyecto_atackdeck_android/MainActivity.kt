package com.example.proyecto_atackdeck_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_atackdeck_android.databinding.ActivityMainBinding
import com.example.proyecto_atackdeck_android.repository.PokemonRepository
import com.example.proyecto_atackdeck_android.BottomNavHelper
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val repository = PokemonRepository() // Conexión a Firebase
    private lateinit var rankingAdapter: RankingAdapter

    private lateinit var auth: FirebaseAuth


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
        binding.tvUserName.text = nombreUsuario


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




        // 1. Configuramos el LayoutManager del RecyclerView para que sea una lista vertical
        binding.rvMetaPokemons.layoutManager = LinearLayoutManager(this)

        // 2. Cargamos los datos desde Firebase
        loadRankingFromFirebase()

        binding.btnViewMoreRanking.setOnClickListener {
            val intent = Intent(this@MainActivity, MetaPokemonActivity::class.java)
            startActivity(intent)
        }
        BottomNavHelper.setupBottomNav(
            activity = this,
            bottomNavigationView = binding.includedBottomNav.root,
            currentItemId = R.id.nav_home
        )
    }

    private fun loadRankingFromFirebase() {
        lifecycleScope.launch {
            // Obtenemos la lista desde Firestore
            val listaPokemons = repository.getSavedPokemons(limite = 4)

            // Instanciamos el Adapter pasándole la lista
            rankingAdapter = RankingAdapter(listaPokemons) { pokemonSeleccionado ->
                // Este código se ejecuta cuando el usuario toca un elemento de la lista
                val intent = Intent(this@MainActivity, PokemonDetailActivity::class.java)
                intent.putExtra(PokemonDetailActivity.EXTRA_POKEMON_ID, pokemonSeleccionado.pokemonId)
                startActivity(intent)
            }

            // Conectamos el Adapter al RecyclerView
            binding.rvMetaPokemons.adapter = rankingAdapter
        }
    }
}