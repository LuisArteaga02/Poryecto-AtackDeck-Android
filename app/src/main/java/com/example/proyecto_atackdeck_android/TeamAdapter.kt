package com.example.proyecto_atackdeck_android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto_atackdeck_android.model.MetaTeam
import com.example.proyecto_atackdeck_android.model.TeamModel

class TeamAdapter(
    private val teamList: List<TeamModel>,
    private val onPokemonClick: (Int) -> Unit,
    private val onSaveClick: ((MetaTeam) -> Unit)? = null// Recibe el ID del Pokémon al tocarlo
) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    class TeamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTeamUser: TextView = view.findViewById(R.id.tvTeamUser)
        val btnSaveTeam: TextView = view.findViewById(R.id.btnSaveTeam)
        val imageViews = listOf<ImageView>(
            view.findViewById(R.id.imgP1),
            view.findViewById(R.id.imgP2),
            view.findViewById(R.id.imgP3),
            view.findViewById(R.id.imgP4),
            view.findViewById(R.id.imgP5),
            view.findViewById(R.id.imgP6)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meta_team, parent, false)
        return TeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val team = teamList[position]


        holder.tvTeamUser.text = team.teamName

        if (team is MetaTeam && onSaveClick != null) {
            holder.btnSaveTeam.visibility = View.VISIBLE
            holder.btnSaveTeam.setOnClickListener {
                onSaveClick(team)
            }
        } else {
            holder.btnSaveTeam.visibility = View.GONE
        }

        // 2. Iterar sobre los 6 espacios de imagen
        for (i in 0 until 6) {
            val imgView = holder.imageViews[i]
            val pokemonData = team.pokemons.getOrNull(i)

            if (pokemonData != null) {
                imgView.visibility = View.VISIBLE

                // Cargar imagen con Glide
                Glide.with(holder.itemView.context)
                    .load(pokemonData.spriteUrl)
                    .into(imgView)

                // Al hacer clic en un Pokémon específico del equipo, abre su detalle
                imgView.setOnClickListener {
                    onPokemonClick(pokemonData.pokemonId)
                }
            } else {
                // Si el equipo tiene menos de 6 Pokémon, ocultamos el espacio sobrante
                imgView.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = teamList.size
}