package com.example.proyecto_atackdeck_android.api

import com.example.proyecto_atackdeck_android.model.Generation
import com.example.proyecto_atackdeck_android.model.Pokemon
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiPokemon {
    @GET("pokemon?limit=10000")
    suspend fun getAllPokemon(): Response<PokemonListResponse>

    @GET("pokemon/{id}")
    suspend fun getPokemonByNumber(@Path("id") id: Int): Response<Pokemon>

    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(@Path("id") id: Int): Response<Pokemon>

    @GET("pokemon/{name}")
    suspend fun getPokemonByName(@Path("name") name: String): Response<Pokemon>
    @GET("generation/{id}")
    suspend fun getGeneration(@Path("id") id: Int): Response<Generation>

    data class PokemonListResponse(
        val results: List<PokemonName>
    )

    data class PokemonName(
        val name: String
    )
}