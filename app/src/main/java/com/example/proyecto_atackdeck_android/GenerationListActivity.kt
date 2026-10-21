package com.example.proyecto_atackdeck_android

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_atackdeck_android.api.RetrofitInstance
import com.example.proyecto_atackdeck_android.databinding.ActivityGenerationListBinding
import kotlinx.coroutines.launch

class GenerationListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenerationListBinding
    private lateinit var adapter: GenerationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGenerationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener la generación seleccionada desde el Intent
        val generationId = intent.getIntExtra("EXTRA_GENERATION_ID", 1)
        binding.tvGenerationTitle.text = "Generación $generationId"

        // Botón Volver
        binding.btnBack.setOnClickListener { finish() }

        // Configurar RecyclerView
        binding.rvGenerationPokemons.layoutManager = LinearLayoutManager(this)

        // Cargar datos
        loadGeneration(generationId)
    }

    private fun loadGeneration(generationId: Int) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.apiInterface.getGeneration(generationId)

                if (response.isSuccessful && response.body() != null) {

                    // Extraemos la lista, pero viene desordenada, así que la ordenamos por su ID interno
                    val rawList = response.body()!!.pokemon_species
                    val sortedList = rawList.sortedBy {
                        it.url.trimEnd('/').substringAfterLast("/").toInt()
                    }

                    // Configuramos el Adapter
                    adapter = GenerationAdapter(sortedList) { pokemonId ->
                        // Al hacer clic, vamos a PokemonDetailActivity pasándole el ID
                        val intent = Intent(this@GenerationListActivity, PokemonDetailActivity::class.java)
                        intent.putExtra(PokemonDetailActivity.EXTRA_POKEMON_ID, pokemonId)
                        startActivity(intent)
                    }

                    binding.rvGenerationPokemons.adapter = adapter
                } else {
                    Toast.makeText(this@GenerationListActivity, "Error al cargar generación", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GenerationListActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}