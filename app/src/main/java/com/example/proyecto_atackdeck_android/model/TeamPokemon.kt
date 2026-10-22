package com.example.proyecto_atackdeck_android.model

data class TeamPokemon(
    val pokemonId: Int = 0,
    val name: String = "",
    val spriteUrl: String = ""
) {
    constructor() : this(0, "", "")
}