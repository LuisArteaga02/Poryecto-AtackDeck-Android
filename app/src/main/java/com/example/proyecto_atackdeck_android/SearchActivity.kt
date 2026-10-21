package com.example.proyecto_atackdeck_android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_atackdeck_android.databinding.ActivitySearchBinding
import com.example.proyecto_atackdeck_android.BottomNavHelper

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Lógica de búsqueda corregida para evitar duplicación
        binding.etSearch.setOnEditorActionListener { view, actionId, _ ->
            // Solo escuchamos la acción explícita de "Buscar" en el teclado
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                realizarBusqueda()

                // Ocultamos el teclado virtual
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        // Configurar clics de las Generaciones
        binding.cardGen1.setOnClickListener { abrirGeneracion(1) }
        binding.cardGen2.setOnClickListener { abrirGeneracion(2) }
        binding.cardGen3.setOnClickListener { abrirGeneracion(3) }
        binding.cardGen4.setOnClickListener { abrirGeneracion(4) }
        binding.cardGen5.setOnClickListener { abrirGeneracion(5) }
        binding.cardGen6.setOnClickListener { abrirGeneracion(6) }
        binding.cardGen7.setOnClickListener { abrirGeneracion(7) }
        binding.cardGen8.setOnClickListener { abrirGeneracion(8) }
        binding.cardGen9.setOnClickListener { abrirGeneracion(9) }

        // Configurar Bottom Navigation
        BottomNavHelper.setupBottomNav(
            activity = this,
            bottomNavigationView = binding.includedBottomNav.root,
            currentItemId = R.id.nav_search
        )
    }

    private fun realizarBusqueda() {
        val query = binding.etSearch.text.toString().trim().lowercase()

        if (query.isNotEmpty()) {
            val intent = Intent(this, PokemonDetailActivity::class.java)
            // Se envía explícitamente el nombre, el ID se ignora en la otra pantalla
            intent.putExtra("EXTRA_POKEMON_NAME", query)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Ingresa un nombre de Pokémon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirGeneracion(generacion: Int) {
        val intent = Intent(this, GenerationListActivity::class.java)
        intent.putExtra("EXTRA_GENERATION_ID", generacion)
        startActivity(intent)
    }
}