package com.example.proyecto_atackdeck_android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_atackdeck_android.databinding.ItemMoveBinding
import com.example.proyecto_atackdeck_android.model.MoveSlot
import com.example.proyecto_atackdeck_android.util.toPokeFormat

class MoveSlotAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<MoveSlot, MoveSlotAdapter.MoveSlotViewHolder>(DiffCallback) {

    inner class MoveSlotViewHolder(private val binding: ItemMoveBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(moveSlot: MoveSlot) {
            binding.tvMoveName.text = moveSlot.move.name.toPokeFormat()
            binding.root.setOnClickListener { onItemClick(moveSlot.move.name) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoveSlotViewHolder {
        val binding = ItemMoveBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MoveSlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoveSlotViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<MoveSlot>() {
        override fun areItemsTheSame(oldItem: MoveSlot, newItem: MoveSlot) =
            oldItem.move.name == newItem.move.name

        override fun areContentsTheSame(oldItem: MoveSlot, newItem: MoveSlot) =
            oldItem == newItem
    }
}