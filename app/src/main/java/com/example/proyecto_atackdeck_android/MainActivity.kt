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
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val repository = PokemonRepository() // Conexión a Firebase
    private lateinit var rankingAdapter: RankingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Configuramos el LayoutManager del RecyclerView para que sea una lista vertical
        binding.rvMetaPokemons.layoutManager = LinearLayoutManager(this)

        // 2. Cargamos los datos desde Firebase
        loadRankingFromFirebase()
    }

    private fun loadRankingFromFirebase() {
        lifecycleScope.launch {
            // Obtenemos la lista desde Firestore
            val listaPokemons = repository.getSavedPokemons()

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