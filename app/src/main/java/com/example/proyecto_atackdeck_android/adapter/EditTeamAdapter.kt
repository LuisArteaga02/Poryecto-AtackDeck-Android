package com.example.proyecto_atackdeck_android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto_atackdeck_android.R
import com.example.proyecto_atackdeck_android.model.TeamPokemon

class EditTeamAdapter(
    private var pokemons: MutableList<TeamPokemon>,
    private val onDeleteClick: (Int, TeamPokemon) -> Unit
) : RecyclerView.Adapter<EditTeamAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvPokeListName)
        val imgPokemon: ImageView = view.findViewById(R.id.imgPokeList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pokemon = pokemons[position]


        holder.tvName.text = pokemon.name.replaceFirstChar { it.uppercase() }

        Glide.with(holder.itemView.context)
            .load(pokemon.spriteUrl)
            .into(holder.imgPokemon)


        holder.itemView.setOnClickListener {
            onDeleteClick(position, pokemon)
        }
    }

    override fun getItemCount(): Int = pokemons.size

    fun updateList(newList: MutableList<TeamPokemon>) {
        pokemons = newList
        notifyDataSetChanged()
    }
}