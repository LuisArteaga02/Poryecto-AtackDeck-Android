package com.example.proyecto_atackdeck_android.model

data class CommunityTeam(
    val teamId: String = "",
    val userId: String = "",
    val userName: String = "",
    val teamName: String = "",
    val description: String = "",
    val pokemons: List<TeamPokemon> = emptyList(),
    val score: Int = 0,
    val upvotedBy: List<String> = emptyList(),
    val downvotedBy: List<String> = emptyList()
)