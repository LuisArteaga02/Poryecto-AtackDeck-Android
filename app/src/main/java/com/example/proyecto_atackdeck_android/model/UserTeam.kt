package com.example.proyecto_atackdeck_android.model

data class UserTeam(
    val teamId: String = "",
    val userId: String = "",
    override val teamName: String = "",
    override val pokemons: List<TeamPokemon> = emptyList()
): TeamModel {
    constructor() : this("", "", "", emptyList())
}