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
import com.example.proyecto_atackdeck_android.adapter.CommunityTeamAdapter
import com.example.proyecto_atackdeck_android.databinding.ActivityCommunityTeamsBinding
import com.example.proyecto_atackdeck_android.repository.PokemonRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CommunityTeamsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityTeamsBinding
    private val repository = PokemonRepository()
    private lateinit var currentUserId: String
    private lateinit var adapter: CommunityTeamAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCommunityTeamsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener el ID del usuario actual de Firebase Auth
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        currentUserId = currentUser.uid

        // Configurar RecyclerView
        binding.rvCommunityTeams.layoutManager = LinearLayoutManager(this)

        // Configurar SwipeRefresh
        binding.swipeRefresh.setOnRefreshListener {
            loadCommunityTeams()
        }

        binding.btnVolver.setOnClickListener { finish() }

        loadCommunityTeams()
    }

    private fun loadCommunityTeams() {
        binding.swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            try {
                val teamsList = repository.getCommunityTeams()

                adapter = CommunityTeamAdapter(
                    teamList = teamsList,
                    currentUserId = currentUserId,
                    onUpvoteClick = { teamClicked ->
                        procesarVoto(teamClicked.teamId, isUpvote = true)
                    },
                    onDownvoteClick = { teamClicked ->
                        procesarVoto(teamClicked.teamId, isUpvote = false)
                    },
                    onPokemonClick = { pokemonId ->
                        val intent =
                            Intent(this@CommunityTeamsActivity, PokemonDetailActivity::class.java)
                        intent.putExtra(PokemonDetailActivity.EXTRA_POKEMON_ID, pokemonId)
                        startActivity(intent)
                    }
                )
                binding.rvCommunityTeams.adapter = adapter
            } catch (e: Exception) {
                Toast.makeText(this@CommunityTeamsActivity, "Error al cargar la comunidad", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun procesarVoto(teamId: String, isUpvote: Boolean) {
        lifecycleScope.launch {
            // Mandamos la orden a Firebase usando las transacciones que creamos en el repositorio
            val exito = repository.voteOnCommunityTeam(teamId, isUpvote)

            if (exito) {
                // Si el voto se registró correctamente, recargamos la lista silenciosamente
                // para que las flechas y la puntuación se actualicen en la pantalla
                val updatedList = repository.getCommunityTeams()
                adapter.updateData(updatedList)
            } else {
                Toast.makeText(this@CommunityTeamsActivity, "Error al registrar el voto", Toast.LENGTH_SHORT).show()
            }
        }
    }
}