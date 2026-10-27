package com.example.proyecto_atackdeck_android.model

import com.google.gson.annotations.SerializedName

data class Pokemon(
    val id: Int,
    val name: String,
    val types: List<TypeSlot>,
    val category: String, // Ej: Seed Pokémon
    val height: Int, // En decímetros (1.2 m -> 12)
    val weight: Int, // En hectogramos (54.0 kg -> 540)
    val abilities: List<AbilitySlot>,
    val stats: List<Stat>, // HP, Attack, Defense, Sp. Attack, Sp. Defense, Speed, Total
    val genderRate: Int,
    val sprites: Sprites,
    val moves: List<MoveSlot>
)

data class TypeSlot(
    val slot: Int,
    val type: Type
)

data class Type(
    val name: String,
    val url: String
)

data class AbilitySlot(
    @SerializedName("is_hidden")
    val isHidden: Boolean,
    val ability: Ability
)

data class Ability(
    val name: String,
    val url: String,
)

data class Stat(
    @SerializedName("base_stat")
    val baseStat: Int
)

data class Sprites(
    @SerializedName("front_default")
    val frontDefault: String?
)

data class MoveSlot(
    val move: Move
)

data class Move(
    val name: String,
    val url: String
)