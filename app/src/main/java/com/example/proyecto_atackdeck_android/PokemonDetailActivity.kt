package com.example.proyecto_atackdeck_android

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.proyecto_atackdeck_android.api.RetrofitInstance
import com.example.proyecto_atackdeck_android.databinding.ActivityPokemonDetailBinding
import kotlinx.coroutines.launch
import android.graphics.Color


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

                    val primaryTypeName = tipos.getOrNull(0)?.type?.name ?: ""
                    binding.tvType1.text = tipos.getOrNull(0)?.type?.name ?: ""
                    binding.tvType1.visibility =
                        if (tipos.isNotEmpty()) View.VISIBLE else View.GONE

                    if (primaryTypeName.isNotEmpty()) {
                        val colorPokemon = getColorByType(primaryTypeName)

                        binding.viewPokemonBackground.backgroundTintList = ColorStateList.valueOf(colorPokemon)
                    }

                    val type2 = tipos.getOrNull(1)?.type?.name
                    if (type2 != null) {
                        binding.tvType2.text = type2
                        binding.tvType2.visibility = View.VISIBLE
                    } else {
                        binding.tvType2.visibility = View.GONE
                    }
                    val habilidades = pokemon.abilities
                    binding.tvAbility1.text = habilidades.getOrNull(0)?.ability?.name ?: "N/A"
                    val ability2 = habilidades.getOrNull(1)?.ability?.name
                    if (ability2 != null) {
                        binding.tvAbility2.text = ability2
                        binding.tvAbility2.visibility = View.VISIBLE
                    } else {
                        // Si no tiene segunda habilidad, ocultamos el TextView
                        binding.tvAbility2.visibility = View.GONE
                    }

                    val statsList = pokemon.stats


                    val hp = statsList.find { it.stat.name == "hp" }?.base_stat ?: 0
                    val attack = statsList.find { it.stat.name == "attack" }?.base_stat ?: 0
                    val defense = statsList.find { it.stat.name == "defense" }?.base_stat ?: 0
                    val spAttack = statsList.find { it.stat.name == "special-attack" }?.base_stat ?: 0
                    val spDefense = statsList.find { it.stat.name == "special-defense" }?.base_stat ?: 0
                    val speed = statsList.find { it.stat.name == "speed" }?.base_stat ?: 0


                    binding.tvHp.text = hp.toString()
                    binding.tvAttack.text = attack.toString()
                    binding.tvDefense.text = defense.toString()
                    binding.tvSpAttack.text = spAttack.toString()
                    binding.tvSpDefense.text = spDefense.toString()
                    binding.tvSpeed.text = speed.toString()



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

private fun getColorByType(type: String): Int {
    return when (type.lowercase()) {
        "normal" -> Color.parseColor("#A8A77A")
        "fire" -> Color.parseColor("#EE8130")
        "water" -> Color.parseColor("#6390F0")
        "electric" -> Color.parseColor("#F7D02C")
        "grass" -> Color.parseColor("#7AC74C")
        "ice" -> Color.parseColor("#96D9D6")
        "fighting" -> Color.parseColor("#C22E28")
        "poison" -> Color.parseColor("#A33EA1")
        "ground" -> Color.parseColor("#E2BF65")
        "flying" -> Color.parseColor("#A98FF3")
        "psychic" -> Color.parseColor("#F95587")
        "bug" -> Color.parseColor("#A6B91A")
        "rock" -> Color.parseColor("#B6A136")
        "ghost" -> Color.parseColor("#735797")
        "dragon" -> Color.parseColor("#6F35FC")
        "dark" -> Color.parseColor("#705848")
        "steel" -> Color.parseColor("#B7B7CE")
        "fairy" -> Color.parseColor("#D685AD")
        else -> Color.parseColor("#777777") // Color por defecto (gris)
    }
}