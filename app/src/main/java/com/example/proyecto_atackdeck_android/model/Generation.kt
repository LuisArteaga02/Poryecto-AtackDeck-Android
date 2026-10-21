package com.example.proyecto_atackdeck_android.model

data class Generation(
    val id: Int,
    val name: String,
    val pokemon_species: List<PokemonSpecies>
)