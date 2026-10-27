package com.example.proyecto_atackdeck_android.repository


import com.example.proyecto_atackdeck_android.model.MetaTeam
import com.example.proyecto_atackdeck_android.model.SavedPokemon
import com.example.proyecto_atackdeck_android.model.UserTeam
import com.example.proyecto_atackdeck_android.model.TeamPokemon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query


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

    suspend fun uploadCommunityTeam(teamName: String, description: String, pokemonsList: List<TeamPokemon>): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            val teamRef = db.collection("community_teams").document()


            val userProfileSnap = db.collection("users_profiles").document(currentUser.uid).get().await()
            val userName = userProfileSnap.getString("userName") ?: currentUser.email?.substringBefore("@") ?: "Usuario"

            val communityTeam = com.example.proyecto_atackdeck_android.model.CommunityTeam(
                teamId = teamRef.id,
                userId = currentUser.uid,
                userName = userName,
                teamName = teamName,
                description = description,
                pokemons = pokemonsList,
                score = 0,
                upvotedBy = emptyList(),
                downvotedBy = emptyList()
            )

            teamRef.set(communityTeam).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    suspend fun getCommunityTeams(): List<com.example.proyecto_atackdeck_android.model.CommunityTeam> {
        return try {
            val snapshot = db.collection("community_teams")
                .orderBy("score", Query.Direction.DESCENDING) // Ordena por los más votados primero
                .get()
                .await()

            snapshot.toObjects(com.example.proyecto_atackdeck_android.model.CommunityTeam::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun voteOnCommunityTeam(teamId: String, isUpvote: Boolean): Boolean {
        val currentUser = auth.currentUser ?: return false
        val userId = currentUser.uid
        val docRef = db.collection("community_teams").document(teamId)

        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)


                val upvotedBy = snapshot.get("upvotedBy") as? List<String> ?: emptyList()
                val downvotedBy = snapshot.get("downvotedBy") as? List<String> ?: emptyList()

                var scoreChange = 0L

                if (isUpvote) {
                    if (userId in upvotedBy) {
                        // Si ya le había dado Upvote, se lo quitamos (lo deja neutral)
                        transaction.update(docRef, "upvotedBy", FieldValue.arrayRemove(userId))
                        scoreChange = -1L
                    } else {
                        // Le damos Upvote
                        transaction.update(docRef, "upvotedBy", FieldValue.arrayUnion(userId))
                        scoreChange = 1L
                        if (userId in downvotedBy) {
                            // Si antes le había dado Downvote, lo quitamos
                            transaction.update(docRef, "downvotedBy", FieldValue.arrayRemove(userId))
                            scoreChange = 2L
                        }
                    }
                } else {

                    if (userId in downvotedBy) {

                        transaction.update(docRef, "downvotedBy", FieldValue.arrayRemove(userId))
                        scoreChange = 1L
                    } else {

                        transaction.update(docRef, "downvotedBy", FieldValue.arrayUnion(userId))
                        scoreChange = -1L
                        if (userId in upvotedBy) {

                            transaction.update(docRef, "upvotedBy", FieldValue.arrayRemove(userId))
                            scoreChange = -2L
                        }
                    }
                }


                transaction.update(docRef, "score", FieldValue.increment(scoreChange))
            }.await()
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

    suspend fun deleteUserTeam(teamId: String): Boolean {
        val currentUser = auth.currentUser ?: return false
        return try {
            db.collection("users_profiles")
                .document(currentUser.uid)
                .collection("user_teams")
                .document(teamId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getUserTeamById(teamId: String): UserTeam? {
        val currentUser = auth.currentUser ?: return null
        return try {
            val snapshot = db.collection("users_profiles")
                .document(currentUser.uid)
                .collection("user_teams")
                .document(teamId)
                .get()
                .await()
            snapshot.toObject(UserTeam::class.java)
        } catch (e: Exception) {
            null
        }
    }
    suspend fun updateTeamInfo(teamId: String, newName: String, pokemonsList: List<TeamPokemon>): Boolean {
        val currentUser = auth.currentUser ?: return false
        return try {
            db.collection("users_profiles")
                .document(currentUser.uid)
                .collection("user_teams")
                .document(teamId)
                .update(
                    mapOf(
                        "teamName" to newName,
                        "pokemons" to pokemonsList
                    )
                ).await()
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

    suspend fun updateUserTeamPokemons(teamId: String, pokemonsList: List<TeamPokemon>): Boolean {
        val currentUser = auth.currentUser ?: return false
        return try {
            db.collection("users_profiles")
                .document(currentUser.uid)
                .collection("user_teams")
                .document(teamId)
                .update("pokemons", pokemonsList)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
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