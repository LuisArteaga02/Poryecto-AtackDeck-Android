package com.example.proyecto_atackdeck_android.model

data class SavedPokemon(
    val pokemonId: Int = 0,
    val name: String = "",
    val spriteUrl: String = "",
    val customTag: String = "",
    val ranking: Int = 0
) {
    constructor() : this(0, "", "", "", 0)
}