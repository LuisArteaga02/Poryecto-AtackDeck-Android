package com.example.proyecto_atackdeck_android.model

data class MetaTeam(
    override val teamName: String = "",
    val ranking: Int = 0,
    override val pokemons: List<TeamPokemon> = emptyList()
) : TeamModel {
    constructor() : this("", 0, emptyList())
}