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
        binding.btnBack.setOnClickListener { finish() }
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

        BottomNavHelper.setupBottomNav(
            activity = this,
            bottomNavigationView = binding.includedBottomNav.root,
            currentItemId = R.id.nav_home
        )
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

                    val tipos = pokemon.types.sortedBy { it.slot }
                    val tipoPrincipal = tipos.getOrNull(0)?.type?.name ?: ""
                    binding.tvType1.text = tipoPrincipal
                    binding.tvType1.visibility = if (tipos.isNotEmpty()) View.VISIBLE else View.GONE

                    val type2 = tipos.getOrNull(1)?.type?.name
                    if (type2 != null) {
                        binding.tvType2.text = type2
                        binding.tvType2.visibility = View.VISIBLE
                    } else {
                        binding.tvType2.visibility = View.GONE
                    }

                    if (tipoPrincipal.isNotEmpty()) {
                        val colorPokemon = getColorByType(tipoPrincipal)
                        binding.viewPokemonBackground.backgroundTintList =
                            ColorStateList.valueOf(colorPokemon)
                    }

                    val habilidades = pokemon.abilities
                    binding.tvAbility1.text = habilidades.getOrNull(0)?.ability?.name ?: "N/A"

                    val ability2 = habilidades.getOrNull(1)?.ability?.name
                    if (ability2 != null) {
                        binding.tvAbility2.text = ability2
                        binding.tvAbility2.visibility = View.VISIBLE
                    } else {
                        binding.tvAbility2.visibility = View.GONE
                    }

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
}