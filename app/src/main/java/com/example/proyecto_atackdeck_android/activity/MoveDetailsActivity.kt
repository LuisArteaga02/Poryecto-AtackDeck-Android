package com.example.proyecto_atackdeck_android.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.proyecto_atackdeck_android.R
import com.example.proyecto_atackdeck_android.api.RetrofitInstance
import com.example.proyecto_atackdeck_android.databinding.ActivityMoveDetailsBinding
import com.example.proyecto_atackdeck_android.util.toPokeFormat
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MoveDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMoveDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMoveDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recupera el movimiento seleccionado
        val moveName = intent.getStringExtra("moveName")
        if (moveName.isNullOrEmpty()) {
            makeSnackbar(R.string.error_unexpected)
            return
        }
        binding.tvMoveTitle.text = moveName.toPokeFormat()
        binding.btnVolver.setOnClickListener { finish() }
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.apiInterface.getMoveByName(moveName)
                if (response.isSuccessful && response.body() != null) {
                    val move = response.body()!!

                    // Llenado de campos
                    binding.tvType.text = move.type.name.toPokeFormat()
                    binding.tvCategory.text = move.category.name.toPokeFormat()
                    binding.tvBP.text = move.power.toString()
                    binding.tvAccuracy.text = move.accuracy.toString()
                    binding.tvPP.text = move.pp.toString()
                    binding.tvPriority.text = move.priority.toString()
                    binding.tvGen.text = move.generation.name.toPokeFormat()
                    binding.tvEffect.text = move.effectEntries.find { it.language.name == "en" }?.effect

                    // Lista de Pokémon

                }
                else {
                    makeSnackbar(R.string.error_pokemon)
                }
            } catch (e: Exception) {
                makeSnackbar(getString(R.string.error_exception, e))
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun makeSnackbar(message: Int) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun makeSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}