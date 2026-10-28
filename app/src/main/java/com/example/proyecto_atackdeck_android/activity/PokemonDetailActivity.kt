package com.example.proyecto_atackdeck_android.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.proyecto_atackdeck_android.adapter.MoveSlotAdapter
import com.example.proyecto_atackdeck_android.api.RetrofitInstance
import com.example.proyecto_atackdeck_android.databinding.ActivityPokemonDetailBinding
import com.example.proyecto_atackdeck_android.model.TeamPokemon
import com.example.proyecto_atackdeck_android.model.UserTeam
import com.example.proyecto_atackdeck_android.repository.PokemonRepository
import com.example.proyecto_atackdeck_android.util.toPokeFormat
import kotlinx.coroutines.launch


class PokemonDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPokemonDetailBinding
    private var pokemonActualParaGuardar: TeamPokemon? = null
    private lateinit var moveAdapter: MoveSlotAdapter

    companion object {
        const val EXTRA_POKEMON_ID = "pokemon_id" // clave para recibir el ID

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPokemonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
                    binding.tvName.text = pokemon.name.toPokeFormat()

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
                        hab?.let { tv.text = "${it.ability.name.toPokeFormat()}${if (it.isHidden) " (hidden)" else ""}" }
                    }

                    // Stats base
                    val statsList = pokemon.stats
                    listOf(
                        binding.tvHpValue,
                        binding.tvAttackValue,
                        binding.tvDefenseValue,
                        binding.tvSpAttackValue,
                        binding.tvSpDefenseValue,
                        binding.tvSpeedValue
                    ).forEachIndexed { i, tv ->
                        val stat = statsList.getOrNull(i)
                        stat?.let { tv.text = it.baseStat.toString() }
                    }
                    binding.tvBstValue.text = statsList.sumOf { it.baseStat }.toString()

                    // Barras de stats
                    listOf(
                        binding.tvHpBar,
                        binding.tvAttackBar,
                        binding.tvDefenseBar,
                        binding.tvSpAttackBar,
                        binding.tvSpDefenseBar,
                        binding.tvSpeedBar
                    ).forEachIndexed { i, bar ->
                        val stat = statsList.getOrNull(i)
                        stat?.let {
                            bar.backgroundTintList = ColorStateList.valueOf(getStatBarColor(it.baseStat))
                            bar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                                matchConstraintPercentWidth = if (it.baseStat >= 200) 1.0f else it.baseStat / 200f
                            }
                        }
                    }

                    // Imagen
                    val spriteUrl = pokemonActualParaGuardar!!.spriteUrl
                    if (spriteUrl.isNotEmpty()) {
                        Glide.with(this@PokemonDetailActivity)
                            .load(spriteUrl)
                            .into(binding.imgSprite)
                    }

                    // Movimientos
                    val moves = pokemon.moves
                    moveAdapter = MoveSlotAdapter { name ->
                        val intent = Intent(this@PokemonDetailActivity, MoveDetailsActivity::class.java)
                        intent.putExtra("moveName", name)
                        startActivity(intent)
                    }
                    binding.rvMoves.adapter = moveAdapter
                    moveAdapter.submitList(pokemon.moves)

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
            "normal" -> "#A8A77A"
            "fire" -> "#EE8130"
            "water" -> "#6390F0"
            "electric" -> "#F7D02C"
            "grass" -> "#7AC74C"
            "ice" -> "#96D9D6"
            "fighting" -> "#C22E28"
            "poison" -> "#A33EA1"
            "ground" -> "#E2BF65"
            "flying" -> "#A98FF3"
            "psychic" -> "#F95587"
            "bug" -> "#A6B91A"
            "rock" -> "#B6A136"
            "ghost" -> "#735797"
            "dragon" -> "#6F35FC"
            "dark" -> "#705848"
            "steel" -> "#B7B7CE"
            "fairy" -> "#D685AD"
            else -> "#777777" // Color por defecto (gris)
        }.toColorInt()
    }

    private fun getStatBarColor(value: Int): Int {
        return when (value) {
            in 1..29 -> "#F34444"
            in 30 .. 59 -> "#FF7F0F"
            in 60 .. 89 -> "#FFDD57"
            in 90 .. 119 -> "#A0E515"
            in 120 .. 149 -> "#23CD5E"
            in 150 .. 255 -> "#00C2B8"
            else -> "#777777"
        }.toColorInt()
    }
}