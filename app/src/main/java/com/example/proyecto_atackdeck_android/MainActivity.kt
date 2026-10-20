package com.example.proyecto_atackdeck_android

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_atackdeck_android.databinding.ActivityMainBinding
import android.content.Intent

class MainActivity : AppCompatActivity() {



    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnprueba.setOnClickListener {
            val intent = Intent(this, PokemonDetailActivity::class.java)
            intent.putExtra(PokemonDetailActivity.EXTRA_POKEMON_ID, 25) // 25 = Pikachu
            startActivity(intent)
        }
        binding.btnprueba2.setOnClickListener {
            val intent = Intent(this, PokemonDetailActivity::class.java)
            intent.putExtra(PokemonDetailActivity.EXTRA_POKEMON_ID, 67) // 25 = Pikachu
            startActivity(intent)
        }
    }


}