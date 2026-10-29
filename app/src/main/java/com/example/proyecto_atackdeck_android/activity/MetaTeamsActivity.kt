package com.example.proyecto_atackdeck_android.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_atackdeck_android.R
import com.example.proyecto_atackdeck_android.adapter.TeamAdapter
import com.example.proyecto_atackdeck_android.databinding.ActivityMetaTeamsBinding
import com.example.proyecto_atackdeck_android.repository.PokemonRepository
import kotlinx.coroutines.launch

class MetaTeamsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMetaTeamsBinding
    private val repository = PokemonRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMetaTeamsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVolver.setOnClickListener { finish() }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.rvMetaTeams.layoutManager = LinearLayoutManager(this)
        binding.swipeRefresh.setOnRefreshListener {
            loadMetaTeams()
        }
        loadMetaTeams()
    }

    private fun loadMetaTeams() {
        binding.swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            try {
                // Trae los equipos (puedes aumentar el límite a 20 o borrarlo para traer todos)
                val teamsList = repository.getMetaTeams(limite = 20)

                val adapter = TeamAdapter(
                    teamList = teamsList,
                    onPokemonClick = { pokemonId ->
                        val intent =
                            Intent(this@MetaTeamsActivity, PokemonDetailActivity::class.java)
                        intent.putExtra(PokemonDetailActivity.EXTRA_POKEMON_ID, pokemonId)
                        startActivity(intent)
                    },
                    onSaveClick = { metaTeamSeleccionado ->
                        lifecycleScope.launch {
                            val exito = repository.saveMetaTeamToUser(metaTeamSeleccionado)
                            if (exito) {
                                Toast.makeText(
                                    this@MetaTeamsActivity,
                                    R.string.team_msg_saved,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@MetaTeamsActivity,
                                    R.string.team_msg_fail,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
                binding.rvMetaTeams.adapter = adapter
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}