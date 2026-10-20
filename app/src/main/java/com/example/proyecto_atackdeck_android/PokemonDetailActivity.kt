package com.example.proyecto_atackdeck_android

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.proyecto_atackdeck_android.api.RetrofitInstance
import com.example.proyecto_atackdeck_android.databinding.ActivityPokemonDetailBinding
import kotlinx.coroutines.launch

class PokemonDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPokemonDetailBinding

    companion object {
        const val EXTRA_POKEMON_ID = "pokemon_id" // clave para recibir el ID
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPokemonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el ID enviado desde otra Activity (default = 1 = Bulbasaur)
        val pokemonId = intent.getIntExtra(EXTRA_POKEMON_ID, 1)

        // Botón volver
        binding.btnBack.setOnClickListener { finish() }

        // Cargar datos
        loadPokemon(pokemonId)
    }

    private fun loadPokemon(id: Int) {
        // Mostrar cargando
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.apiInterface.getPokemonDetail(id)

                if (response.isSuccessful) {
                    val pokemon = response.body()!!

                    // ID y Nombre
                    binding.tvId.text = "#${pokemon.id.toString().padStart(3, '0')}"
                    binding.tvName.text = pokemon.name

                    // Altura: la API da decímetros → convertir a metros
                    val heightM = pokemon.height / 10.0
                    binding.tvHeight.text = "${"%.1f".format(heightM)} m"

                    // Peso: la API da hectogramos → convertir a kg
                    val weightKg = pokemon.weight / 10.0
                    binding.tvWeight.text = "${"%.1f".format(weightKg)} kg"

                    // Tipos
                    val tipos = pokemon.types.sortedBy { it.slot }

                    binding.tvType1.text = tipos.getOrNull(0)?.type?.name ?: ""
                    binding.tvType1.visibility =
                        if (tipos.isNotEmpty()) View.VISIBLE else View.GONE

                    val type2 = tipos.getOrNull(1)?.type?.name
                    if (type2 != null) {
                        binding.tvType2.text = type2
                        binding.tvType2.visibility = View.VISIBLE
                    } else {
                        binding.tvType2.visibility = View.GONE
                    }

                    // Sprite con Glide
                    val spriteUrl = pokemon.sprites.frontDefault
                    if (!spriteUrl.isNullOrEmpty()) {
                        Glide.with(this@PokemonDetailActivity)
                            .load(spriteUrl)
                            .into(binding.imgSprite)
                    }

                } else {
                    Toast.makeText(this@PokemonDetailActivity,
                        "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@PokemonDetailActivity,
                    "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}