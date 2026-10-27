package com.example.proyecto_atackdeck_android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto_atackdeck_android.R
import com.example.proyecto_atackdeck_android.model.MetaTeam
import com.example.proyecto_atackdeck_android.model.TeamModel
import com.example.proyecto_atackdeck_android.model.UserTeam

class TeamAdapter(
    private val teamList: List<TeamModel>,
    private val onPokemonClick: (Int) -> Unit,
    private val onSaveClick: ((MetaTeam) -> Unit)? = null,
    private val onEditClick: ((UserTeam) -> Unit)? = null,
    private val onDeleteClick: ((UserTeam) -> Unit)? = null,
    private val onPublishClick: ((UserTeam) -> Unit)? = null
) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    class TeamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTeamUser: TextView = view.findViewById(R.id.tvTeamUser)
        val btnSaveTeam: TextView = view.findViewById(R.id.btnSaveTeam)

        val btnEditTeam: TextView = view.findViewById(R.id.btnEditTeam)
        val btnDeleteTeam: TextView = view.findViewById(R.id.btnDeleteTeam)
        val btnPublishTeam: TextView = view.findViewById(R.id.btnPublishTeam)
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

        when (team) {
            is MetaTeam -> {
                if (onSaveClick != null) {
                    holder.btnSaveTeam.visibility = View.VISIBLE
                    holder.btnSaveTeam.setOnClickListener { onSaveClick.invoke(team) }

                }
                holder.btnEditTeam.visibility = View.GONE
                holder.btnDeleteTeam.visibility = View.GONE
                holder.btnPublishTeam.visibility = View.GONE
            }
            is UserTeam -> {
                holder.btnSaveTeam.visibility = View.GONE

                if (onEditClick != null) {
                    holder.btnEditTeam.visibility = View.VISIBLE
                    holder.btnEditTeam.setOnClickListener { onEditClick.invoke(team) }
                }
                if (onDeleteClick != null) {
                    holder.btnDeleteTeam.visibility = View.VISIBLE
                    holder.btnDeleteTeam.setOnClickListener { onDeleteClick.invoke(team) }
                }
                if (onPublishClick != null) {
                    holder.btnPublishTeam.visibility = View.VISIBLE
                    holder.btnPublishTeam.setOnClickListener { onPublishClick.invoke(team) }
                }else {
                    holder.btnPublishTeam.visibility = View.GONE
                }
            }
        }


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