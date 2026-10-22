package com.example.proyecto_atackdeck_android.repository


import com.example.proyecto_atackdeck_android.model.MetaTeam
import com.example.proyecto_atackdeck_android.model.SavedPokemon
import com.example.proyecto_atackdeck_android.model.UserTeam
import com.example.proyecto_atackdeck_android.model.TeamPokemon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PokemonRepository {


    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
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

    suspend fun saveUserTeam(teamName: String, pokemonsList: List<TeamPokemon>): Boolean {
        val currentUser = auth.currentUser ?: return false
        val userId = currentUser.uid

        return try {
            // Creamos una referencia con ID automático dentro de la subcolección del usuario
            val teamRef = db.collection("users_profiles")
                .document(userId)
                .collection("user_teams")
                .document()

            val newTeam = UserTeam(
                teamId = teamRef.id,
                userId = userId,
                teamName = teamName,
                pokemons = pokemonsList
            )

            teamRef.set(newTeam).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getUserTeams(): List<UserTeam> {
        val currentUser = auth.currentUser ?: return emptyList()
        val userId = currentUser.uid

        return try {
            val snapshot = db.collection("users_profiles")
                .document(userId)
                .collection("user_teams")
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(UserTeam::class.java) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun saveMetaTeamToUser(metaTeam: com.example.proyecto_atackdeck_android.model.MetaTeam): Boolean {
        val currentUser = auth.currentUser ?: return false
        val userId = currentUser.uid

        return try {
            val teamRef = db.collection("users_profiles")
                .document(userId)
                .collection("user_teams")
                .document()

            val newUserTeam = com.example.proyecto_atackdeck_android.model.UserTeam(
                teamId = teamRef.id,
                userId = userId,
                teamName = metaTeam.teamName, // Mantiene el nombre del equipo meta
                pokemons = metaTeam.pokemons
            )

            teamRef.set(newUserTeam).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getSavedPokemons(limite: Long? = null): List<SavedPokemon> {
        return try {
            // Empezamos armando la consulta básica
            var query = pokemonCollection.orderBy("ranking")

            // Si la pantalla nos pide un límite, se lo agregamos a la consulta
            if (limite != null) {
                query = query.limit(limite)
            }

            // Ejecutamos la consulta
            val snapshot = query.get().await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(SavedPokemon::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private val teamsCollection = db.collection("meta_teams")

    // Obtener la lista de equipos Meta
    suspend fun getMetaTeams(limite: Long? = null): List<MetaTeam> {
        return try {
            var query = teamsCollection.orderBy("ranking")
            if (limite != null) {
                query = query.limit(limite)
            }
            val snapshot = query.get().await()
            snapshot.documents.mapNotNull { it.toObject(MetaTeam::class.java) }
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