package com.example.proyecto_atackdeck_android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto_atackdeck_android.model.SavedPokemon

class RankingAdapter(
    private val pokemonList: List<SavedPokemon>,
    private val onItemClick: (SavedPokemon) -> Unit
) : RecyclerView.Adapter<RankingAdapter.PokemonViewHolder>() {

    class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRankPosition: TextView = view.findViewById(R.id.tvRankPosition)
        val tvPokemonName: TextView = view.findViewById(R.id.tvPokemonName)
        val imgPokemon: ImageView = view.findViewById(R.id.imgPokemon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon_ranking, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemonList[position]

        // Asignamos la posición en el ranking (1, 2, 3...)
        holder.tvRankPosition.text = pokemon.ranking.toString()

        // Capitalizamos la primera letra del nombre
        holder.tvPokemonName.text = pokemon.name.replaceFirstChar { it.uppercase() }


        Glide.with(holder.itemView.context)
            .load(pokemon.spriteUrl)
            .into(holder.imgPokemon)


        holder.itemView.setOnClickListener {
            onItemClick(pokemon)
        }
    }

    override fun getItemCount(): Int = pokemonList.size
}