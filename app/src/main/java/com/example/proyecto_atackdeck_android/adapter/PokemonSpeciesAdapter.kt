package com.example.proyecto_atackdeck_android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_atackdeck_android.databinding.ItemPokemonListBinding
import com.example.proyecto_atackdeck_android.model.PokemonSpecies
import com.example.proyecto_atackdeck_android.util.toPokeFormat

class PokemonSpeciesAdapter(
    private val onItemClick: (Int) -> Unit
): ListAdapter<PokemonSpecies, PokemonSpeciesAdapter.PokemonSpeciesViewHolder>(DiffCallback) {
    inner class PokemonSpeciesViewHolder(private val binding: ItemPokemonListBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pokemon: PokemonSpecies) {
            binding.tvPokeListName.text = pokemon.name.toPokeFormat()
            binding.root.setOnClickListener {
                onItemClick(pokemon.url.trimEnd('/').substringAfterLast("/").toInt())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonSpeciesViewHolder {
        val binding = ItemPokemonListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PokemonSpeciesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PokemonSpeciesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    private object DiffCallback : DiffUtil.ItemCallback<PokemonSpecies>() {
        override fun areItemsTheSame(oldItem: PokemonSpecies, newItem: PokemonSpecies) =
            oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: PokemonSpecies, newItem: PokemonSpecies) =
            oldItem == newItem
    }
}