package com.example.proyecto_atackdeck_android

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_atackdeck_android.databinding.ActivityEditUserTeamBinding
import com.example.proyecto_atackdeck_android.model.TeamPokemon
import com.example.proyecto_atackdeck_android.repository.PokemonRepository
import kotlinx.coroutines.launch

class EditUserTeamActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditUserTeamBinding
    private val repository = PokemonRepository()
    private var teamId: String = ""
    private var currentPokemons = mutableListOf<TeamPokemon>()
    private lateinit var adapter: EditTeamAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditUserTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir el ID del equipo desde el UserDeckActivity
        teamId = intent.getStringExtra("EXTRA_TEAM_ID") ?: ""

        if (teamId.isEmpty()) {
            Toast.makeText(this, "Error al cargar el equipo", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        adapter = EditTeamAdapter(currentPokemons) { position, pokemon ->
            // Cuadro de confirmación antes de borrar
            AlertDialog.Builder(this)
                .setTitle("Quitar Pokémon")
                .setMessage("¿Deseas quitar a ${pokemon.name.uppercase()} del equipo?")
                .setPositiveButton("Sí, quitar") { _, _ ->
                    currentPokemons.removeAt(position) // Borramos localmente
                    adapter.updateList(currentPokemons) // Refrescamos la lista visual
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        binding.rvEditTeamPokemons.layoutManager = LinearLayoutManager(this)
        binding.rvEditTeamPokemons.adapter = adapter

        // Cargar los datos desde Firebase
        cargarDatosDelEquipo()

        // Guardar en Firebase
        binding.btnSaveEditedTeam.setOnClickListener {
            guardarCambios()
        }

    }
    private fun cargarDatosDelEquipo() {
        lifecycleScope.launch {
            val team = repository.getUserTeamById(teamId)
            if (team != null) {
                binding.etEditTeamName.setText(team.teamName)
                currentPokemons.clear()
                currentPokemons.addAll(team.pokemons)
                adapter.updateList(currentPokemons)
            } else {
                Toast.makeText(this@EditUserTeamActivity, "No se encontró el equipo", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    private fun guardarCambios() {
        val nuevoNombre = binding.etEditTeamName.text.toString().trim()

        if (nuevoNombre.isEmpty()) {
            Toast.makeText(this, "El nombre del equipo no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val exito = repository.updateTeamInfo(teamId, nuevoNombre, currentPokemons)
            if (exito) {
                Toast.makeText(this@EditUserTeamActivity, "Equipo actualizado", Toast.LENGTH_SHORT).show()
                finish() // Regresa automáticamente al perfil
            } else {
                Toast.makeText(this@EditUserTeamActivity, "Error al guardar cambios", Toast.LENGTH_SHORT).show()
            }
        }
    }
}