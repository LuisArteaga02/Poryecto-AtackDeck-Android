package com.example.proyecto_atackdeck_android.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_atackdeck_android.R
import com.example.proyecto_atackdeck_android.adapter.EditTeamAdapter
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
            Toast.makeText(this, R.string.error_load, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        adapter = EditTeamAdapter(currentPokemons) { position, pokemon ->
            // Cuadro de confirmación antes de borrar
            AlertDialog.Builder(this)
                .setTitle(R.string.team_alert_remove_title)
                .setMessage(getString(R.string.team_alert_message, pokemon.name.uppercase()))
                .setPositiveButton(R.string.action_yes) { _, _ ->
                    currentPokemons.removeAt(position) // Borramos localmente
                    adapter.updateList(currentPokemons) // Refrescamos la lista visual
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
        }
        binding.rvEditTeamPokemons.layoutManager = LinearLayoutManager(this)
        binding.rvEditTeamPokemons.adapter = adapter
        binding.btnVolver.setOnClickListener { finish() }

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
                Toast.makeText(this@EditUserTeamActivity, R.string.team_notfound, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    private fun guardarCambios() {
        val nuevoNombre = binding.etEditTeamName.text.toString().trim()

        if (nuevoNombre.isEmpty()) {
            Toast.makeText(this, R.string.team_msg_empty, Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val exito = repository.updateTeamInfo(teamId, nuevoNombre, currentPokemons)
            if (exito) {
                Toast.makeText(this@EditUserTeamActivity, R.string.team_msg_updated, Toast.LENGTH_SHORT).show()
                finish() // Regresa automáticamente al perfil
            } else {
                Toast.makeText(this@EditUserTeamActivity, R.string.error_unexpected, Toast.LENGTH_SHORT).show()
            }
        }
    }
}