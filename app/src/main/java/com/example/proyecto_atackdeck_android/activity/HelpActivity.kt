package com.example.proyecto_atackdeck_android.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_atackdeck_android.databinding.ActivityAboutBinding

class HelpActivity : AppCompatActivity() {


    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnVolver.setOnClickListener { finish() }

    }


}