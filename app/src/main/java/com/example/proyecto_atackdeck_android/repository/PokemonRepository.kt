package com.example.proyecto_atackdeck_android.repository


import com.example.proyecto_atackdeck_android.model.SavedPokemon
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PokemonRepository {


    private val db = FirebaseFirestore.getInstance()
    private val pokemonCollection = db.collection("saved_pokemons")


    suspend fun savePokemon(pokemon: SavedPokemon): Boolean {
        return try {
            pokemonCollection.document(pokemon.pokemonId.toString())
                .set(pokemon)
                .await() // Ya no dará error con la nueva dependencia
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    suspend fun getSavedPokemons(): List<SavedPokemon> {
        return try {
            val snapshot = pokemonCollection
                .orderBy("ranking")
                
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(SavedPokemon::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    suspend fun deletePokemon(pokemonId: Int): Boolean {
        return try {
            pokemonCollection.document(pokemonId.toString())
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}