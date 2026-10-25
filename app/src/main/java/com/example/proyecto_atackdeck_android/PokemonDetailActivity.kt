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
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.proyecto_atackdeck_android.model.TeamPokemon
import com.example.proyecto_atackdeck_android.model.UserTeam
import com.example.proyecto_atackdeck_android.repository.PokemonRepository
import com.example.proyecto_atackdeck_android.util.toNaturalFormat
import androidx.core.graphics.toColorInt


class PokemonDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPokemonDetailBinding
    private var pokemonActualParaGuardar: TeamPokemon? = null

    companion object {
        const val EXTRA_POKEMON_ID = "pokemon_id" // clave para recibir el ID

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPokemonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnVolver.setOnClickListener { finish() }
        binding.btnAddToTeam.isEnabled = false
        binding.btnAddToTeam.setOnClickListener {
            mostrarDialogoEquipos()
        }

        val pokemonName = intent.getStringExtra("EXTRA_POKEMON_NAME")
        val pokemonId = intent.getIntExtra(EXTRA_POKEMON_ID, -1)

        if (!pokemonName.isNullOrEmpty()) {
            descargarDatosPokemon(nombre = pokemonName, id = -1)
        } else if (pokemonId != -1) {
            descargarDatosPokemon(nombre = null, id = pokemonId)
        } else {
            Toast.makeText(this, "Error: Pokémon no recibido", Toast.LENGTH_SHORT).show()
            finish()
        }


    }

    private fun descargarDatosPokemon(nombre: String?, id: Int) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Dependiendo de lo que recibimos, usamos un endpoint u otro
                val response = if (nombre != null) {
                    RetrofitInstance.apiInterface.getPokemonByName(nombre)
                } else {
                    RetrofitInstance.apiInterface.getPokemonDetail(id)
                }

                if (response.isSuccessful && response.body() != null) {
                    val pokemon = response.body()!!

                    // 1. GUARDAMOS EL POKÉMON EN LA VARIABLE GLOBAL DE FORMA SEGURA
                    pokemonActualParaGuardar = TeamPokemon(
                        pokemonId = pokemon.id,
                        name = pokemon.name,
                        spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${pokemon.id}.png"
                    )

                    // 2. ENCENDEMOS EL BOTÓN PORQUE YA ESTÁ LISTO
                    binding.btnAddToTeam.isEnabled = true

                    // 3. LLENAMOS LA PANTALLA
                    binding.tvId.text = "#${pokemon.id.toString().padStart(3, '0')}"
                    binding.tvName.text = pokemon.name.replaceFirstChar { it.uppercase() }

                    val heightM = pokemon.height / 10.0
                    binding.tvHeight.text = "${"%.1f".format(heightM)} m"

                    val weightKg = pokemon.weight / 10.0
                    binding.tvWeight.text = "${"%.1f".format(weightKg)} kg"

                    // Tipos
                    val tipos = pokemon.types
                    val tipo1 = tipos.getOrNull(0)?.type?.name ?: "???"
                    binding.tvType1.text = tipo1
                    binding.tvType1.backgroundTintList = ColorStateList.valueOf(getColorByType(tipo1))

                    val tipo2 = tipos.getOrNull(1)?.type?.name
                    if (!tipo2.isNullOrEmpty()) {
                        binding.tvType2.text = tipo2
                        binding.tvType2.backgroundTintList = ColorStateList.valueOf(getColorByType(tipo2))
                        binding.tvType2.visibility = View.VISIBLE
                    } else {
                        binding.tvType2.visibility = View.GONE
                    }

                    if (tipo1.isNotEmpty()) {
                        val colorPokemon = getColorByType(tipo1)
                        binding.viewPokemonBackground.backgroundTintList =
                            ColorStateList.valueOf(colorPokemon)
                    }

                    // Habilidades
                    val habilidades = pokemon.abilities

                    listOf(binding.tvAbility1, binding.tvAbility2, binding.tvAbility3).forEachIndexed { i, tv ->
                        val hab = habilidades.getOrNull(i)
                        tv.visibility = if (hab != null) View.VISIBLE else View.GONE
                        hab?.let { tv.text = if (it.isHidden) "${it.ability.name.toNaturalFormat()} (hidden)" else it.ability.name.toNaturalFormat() }
                    }

                    // Stats
                    val statsList = pokemon.stats
                    binding.tvHp.text =
                        statsList.find { it.stat.name == "hp" }?.base_stat?.toString() ?: "0"
                    binding.tvAttack.text =
                        statsList.find { it.stat.name == "attack" }?.base_stat?.toString() ?: "0"
                    binding.tvDefense.text =
                        statsList.find { it.stat.name == "defense" }?.base_stat?.toString() ?: "0"
                    binding.tvSpAttack.text =
                        statsList.find { it.stat.name == "special-attack" }?.base_stat?.toString()
                            ?: "0"
                    binding.tvSpDefense.text =
                        statsList.find { it.stat.name == "special-defense" }?.base_stat?.toString()
                            ?: "0"
                    binding.tvSpeed.text =
                        statsList.find { it.stat.name == "speed" }?.base_stat?.toString() ?: "0"

                    val spriteUrl = pokemonActualParaGuardar!!.spriteUrl
                    if (spriteUrl.isNotEmpty()) {
                        Glide.with(this@PokemonDetailActivity)
                            .load(spriteUrl)
                            .into(binding.imgSprite)
                    }

                } else {
                    if (response.code() == 404 && nombre != null) {
                        Toast.makeText(
                            this@PokemonDetailActivity,
                            "Pokémon '$nombre' no encontrado",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@PokemonDetailActivity,
                            "Error: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    finish()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@PokemonDetailActivity,
                    "Error de red: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun mostrarDialogoEquipos() {
        val pokemonToSave = pokemonActualParaGuardar
        if (pokemonToSave == null) {
            Toast.makeText(this, "Espera a que el Pokémon termine de cargar", Toast.LENGTH_SHORT)
                .show()
            return
        }

        lifecycleScope.launch {
            val repository = PokemonRepository()
            val userTeams = repository.getUserTeams()

            val menuOptions = userTeams.map { it.teamName }.toMutableList()
            menuOptions.add("+ Crear Nuevo Equipo")

            AlertDialog.Builder(this@PokemonDetailActivity)
                .setTitle("Agregar a Equipo")
                .setItems(menuOptions.toTypedArray()) { _, indexSeleccionado ->
                    if (indexSeleccionado == menuOptions.size - 1) {
                        mostrarDialogoCrearEquipo(pokemonToSave, repository)
                    } else {
                        val equipoSeleccionado = userTeams[indexSeleccionado]
                        agregarPokemonAEquipoExistente(
                            equipoSeleccionado,
                            pokemonToSave,
                            repository
                        )
                    }
                }
                .show()
        }
    }

    private fun mostrarDialogoCrearEquipo(pokemon: TeamPokemon, repository: PokemonRepository) {
        val input = EditText(this)
        input.hint = "Escribe el nombre del equipo"

        AlertDialog.Builder(this)
            .setTitle("Nuevo Equipo")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val teamName = input.text.toString().trim()
                if (teamName.isNotEmpty()) {
                    lifecycleScope.launch {
                        val exito = repository.saveUserTeam(teamName, listOf(pokemon))
                        if (exito) {
                            Toast.makeText(
                                this@PokemonDetailActivity,
                                "Guardado en $teamName",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun agregarPokemonAEquipoExistente(
        team: UserTeam,
        pokemon: TeamPokemon,
        repository: PokemonRepository
    ) {
        // Validación estricta: No permitir más de 6 Pokémon
        if (team.pokemons.size >= 6) {
            Toast.makeText(
                this,
                "El equipo '${team.teamName}' ya está lleno (Máximo 6)",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val pokemonsActualizados = team.pokemons.toMutableList()
        pokemonsActualizados.add(pokemon)

        lifecycleScope.launch {
            val exito = repository.updateUserTeamPokemons(team.teamId, pokemonsActualizados)
            if (exito) {
                Toast.makeText(
                    this@PokemonDetailActivity,
                    "Agregado a ${team.teamName}",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@PokemonDetailActivity,
                    "Error al actualizar el equipo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun getColorByType(type: String): Int {
        return when (type.lowercase()) {
            "normal" -> "#A8A77A".toColorInt()
            "fire" -> "#EE8130".toColorInt()
            "water" -> "#6390F0".toColorInt()
            "electric" -> "#F7D02C".toColorInt()
            "grass" -> "#7AC74C".toColorInt()
            "ice" -> "#96D9D6".toColorInt()
            "fighting" -> "#C22E28".toColorInt()
            "poison" -> "#A33EA1".toColorInt()
            "ground" -> "#E2BF65".toColorInt()
            "flying" -> "#A98FF3".toColorInt()
            "psychic" -> "#F95587".toColorInt()
            "bug" -> "#A6B91A".toColorInt()
            "rock" -> "#B6A136".toColorInt()
            "ghost" -> "#735797".toColorInt()
            "dragon" -> "#6F35FC".toColorInt()
            "dark" -> "#705848".toColorInt()
            "steel" -> "#B7B7CE".toColorInt()
            "fairy" -> "#D685AD".toColorInt()
            else -> "#777777".toColorInt() // Color por defecto (gris)
        }
    }
}