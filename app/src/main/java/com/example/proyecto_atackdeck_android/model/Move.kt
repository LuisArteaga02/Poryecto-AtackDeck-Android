package com.example.proyecto_atackdeck_android.model

import com.google.gson.annotations.SerializedName

data class MoveDetail(
    val name: String,
    val type: Type,
    @SerializedName("damage_class")
    val category: Category,
    val power: Int,
    val accuracy: Int,
    val pp: Int,
    val priority: Int,
    @SerializedName("effect_entries")
    val effectEntries: List<EffectEntry>,
    @SerializedName("learned_by_pokemon")
    val learnedBy: List<PokemonSpecies>
)

data class Category(
    val name: String
)

data class EffectEntry(
    val effect: String,
    val language: Lang
)

data class Lang(
    val name: String
)