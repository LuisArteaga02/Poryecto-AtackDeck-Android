package com.example.proyecto_atackdeck_android.model

data class Pokemon(
    val id: Int,
    val name: String,
    val type1: Type,
    val type2: Type,
    val category: String, // Ej: Seed Pokémon
    val height: Int, // En decímetros (1.2 m -> 12)
    val weight: Int, // En hectogramos (54.0 kg -> 540)
    val ability1: Ability,
    val ability2: Ability,
    val abilityHidden: Ability,
    val baseStats: List<Int>, // HP, Attack, Defense, Sp. Attack, Sp. Defense, Speed, Total
    val genderRate: Int,
    val sprite: String
)
