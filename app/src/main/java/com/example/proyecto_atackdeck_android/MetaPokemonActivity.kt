package com.example.proyecto_atackdeck_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_atackdeck_android.databinding.ActivityMetaPokemonBinding // <-- Nombre corregido
import com.example.proyecto_atackdeck_android.repository.PokemonRepository
import kotlinx.coroutines.launch

class MetaPokemonActivity : AppCompatActivity() {

    // 1. El tipo de dato debe ser la clase generada por el XML (ActivityMetaPokemonBinding)
    private lateinit var binding: ActivityMetaPokemonBinding

    private val repository = PokemonRepository() // Conexión a Firebase
    private lateinit var rankingAdapter: RankingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 2. Inicializamos el binding y lo pasamos al setContentView
        binding = ActivityMetaPokemonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnVolver.setOnClickListener { finish() }
        // 3. Usamos binding.main en lugar de findViewById
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 4. Agregamos el LayoutManager (Obligatorio para que el RecyclerView se vea)
        binding.rvMetaPokemons.layoutManager = LinearLayoutManager(this)

        // 5. Llamamos a la función para que descargue los datos
        loadRankingFromFirebase()
    }

    private fun loadRankingFromFirebase() {
        lifecycleScope.launch {
            // Obtenemos la lista desde Firestore (puedes subir el límite a 20 o borrar el parámetro para traer todos)
            val listaPokemons = repository.getSavedPokemons(limite = 20)

            // Instanciamos el Adapter pasándole la lista
            rankingAdapter = RankingAdapter(listaPokemons) { pokemonSeleccionado ->
                val intent = Intent(this@MetaPokemonActivity, PokemonDetailActivity::class.java)
                intent.putExtra(PokemonDetailActivity.EXTRA_POKEMON_ID, pokemonSeleccionado.pokemonId)
                startActivity(intent)
            }

            // Conectamos el Adapter al RecyclerView
            binding.rvMetaPokemons.adapter = rankingAdapter
        }
    }
}