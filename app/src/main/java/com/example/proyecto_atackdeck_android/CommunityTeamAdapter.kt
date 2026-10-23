package com.example.proyecto_atackdeck_android

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto_atackdeck_android.model.CommunityTeam

class CommunityTeamAdapter(
    private var teamList: List<CommunityTeam>,
    private val currentUserId: String,
    private val onUpvoteClick: (CommunityTeam) -> Unit,
    private val onDownvoteClick: (CommunityTeam) -> Unit,
    private val onPokemonClick: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<CommunityTeamAdapter.CommunityViewHolder>() {

    class CommunityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCommunityTeamName: TextView = view.findViewById(R.id.tvCommunityTeamName)
        val tvCommunityAuthor: TextView = view.findViewById(R.id.tvCommunityAuthor)
        val tvCommunityDescription: TextView = view.findViewById(R.id.tvCommunityDescription)
        val tvTeamScore: TextView = view.findViewById(R.id.tvTeamScore)

        val btnUpvote: ImageView = view.findViewById(R.id.btnUpvote)
        val btnDownvote: ImageView = view.findViewById(R.id.btnDownvote)

        val imageViews = listOf<ImageView>(
            view.findViewById(R.id.imgP1),
            view.findViewById(R.id.imgP2),
            view.findViewById(R.id.imgP3),
            view.findViewById(R.id.imgP4),
            view.findViewById(R.id.imgP5),
            view.findViewById(R.id.imgP6)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comunnity_team, parent, false)
        return CommunityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val team = teamList[position]

        // 1. Cargar textos
        holder.tvCommunityTeamName.text = team.teamName
        holder.tvCommunityAuthor.text = "Subido por ${team.userName}"
        holder.tvTeamScore.text = team.score.toString()

        if (team.description.isNotEmpty()) {
            holder.tvCommunityDescription.text = team.description
            holder.tvCommunityDescription.visibility = View.VISIBLE
        } else {
            holder.tvCommunityDescription.visibility = View.GONE
        }


        val colorGray = Color.parseColor("#9CA3AF")
        val colorUpvote = Color.parseColor("#FF4500")
        val colorDownvote = Color.parseColor("#7193FF")


        when {
            team.upvotedBy.contains(currentUserId) -> {
                holder.btnUpvote.imageTintList = ColorStateList.valueOf(colorUpvote)
                holder.btnDownvote.imageTintList = ColorStateList.valueOf(colorGray)
                holder.tvTeamScore.setTextColor(colorUpvote)
            }
            team.downvotedBy.contains(currentUserId) -> {
                holder.btnUpvote.imageTintList = ColorStateList.valueOf(colorGray)
                holder.btnDownvote.imageTintList = ColorStateList.valueOf(colorDownvote)
                holder.tvTeamScore.setTextColor(colorDownvote)
            }
            else -> {
                // Neutral
                holder.btnUpvote.imageTintList = ColorStateList.valueOf(colorGray)
                holder.btnDownvote.imageTintList = ColorStateList.valueOf(colorGray)
                holder.tvTeamScore.setTextColor(Color.WHITE)
            }
        }


        holder.btnUpvote.setOnClickListener {
            onUpvoteClick(team)
        }
        holder.btnDownvote.setOnClickListener {
            onDownvoteClick(team)
        }

        // 4. Cargar los 6 Pokémon con Glide
        for (i in 0 until 6) {
            val imgView = holder.imageViews[i]
            val pokemonData = team.pokemons.getOrNull(i)

            if (pokemonData != null) {
                imgView.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(pokemonData.spriteUrl)
                    .into(imgView)

                // Clic opcional para ver detalles
                imgView.setOnClickListener {
                    onPokemonClick?.invoke(pokemonData.pokemonId)
                }
            } else {
                imgView.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = teamList.size


    fun updateData(newList: List<CommunityTeam>) {
        teamList = newList
        notifyDataSetChanged()
    }
}