package com.example.proyecto_atackdeck_android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto_atackdeck_android.model.PokemonSpecies

class GenerationAdapter(
    private val pokemonList: List<PokemonSpecies>,
    private val onItemClick: (Int) -> Unit // Pasamos el ID cuando hacen clic
) : RecyclerView.Adapter<GenerationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvPokeListName)
        val imgPokemon: ImageView = view.findViewById(R.id.imgPokeList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pokemon = pokemonList[position]

        // Nombre con la primera letra mayúscula
        holder.tvName.text = pokemon.name.replaceFirstChar { it.uppercase() }

        // Extraer el ID de la URL (ej: "https://pokeapi.co/api/v2/pokemon-species/25/" -> "25")
        val pokemonId = pokemon.url.trimEnd('/').substringAfterLast("/").toInt()

        // Armar la URL oficial del sprite usando ese ID
        val spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"

        // Cargar imagen
        Glide.with(holder.itemView.context)
            .load(spriteUrl)
            .into(holder.imgPokemon)

        // Clic en la fila
        holder.itemView.setOnClickListener {
            onItemClick(pokemonId)
        }
    }

    override fun getItemCount(): Int = pokemonList.size
}