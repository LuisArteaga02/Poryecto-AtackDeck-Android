package com.example.proyecto_atackdeck_android.activity

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_atackdeck_android.util.BottomNavHelper
import com.example.proyecto_atackdeck_android.R
import com.example.proyecto_atackdeck_android.databinding.ActivitySearchBinding
import com.example.proyecto_atackdeck_android.api.RetrofitInstance

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
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
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

        cargarSugerenciasDePokemon()
    }

    override fun onResume() {
        super.onResume()
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
            Toast.makeText(this, R.string.search_msg_invalid, Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarSugerenciasDePokemon() {
        lifecycleScope.launch {
            try {
                // Descargamos la lista completa de nombres
                val response = RetrofitInstance.apiInterface.getAllPokemon()

                if (response.isSuccessful && response.body() != null) {
                    // Extraemos solo los nombres y los pasamos a una lista de Strings
                    val nombresPokemon = response.body()!!.results.map { it.name }

                    // Creamos el adaptador nativo de Android para listas desplegables
                    val adapter = ArrayAdapter(
                        this@SearchActivity,
                        android.R.layout.simple_dropdown_item_1line, // Diseño por defecto de Android
                        nombresPokemon
                    )


                    binding.etSearch.setAdapter(adapter)


                    binding.etSearch.setOnItemClickListener { parent, view, position, id ->

                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)


                        realizarBusqueda()
                    }
                }
            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }

    private fun abrirGeneracion(generacion: Int) {
        val intent = Intent(this, GenerationListActivity::class.java)
        intent.putExtra("EXTRA_GENERATION_ID", generacion)
        startActivity(intent)
    }
}