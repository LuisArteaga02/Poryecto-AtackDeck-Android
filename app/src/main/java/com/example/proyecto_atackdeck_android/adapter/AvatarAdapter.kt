package com.example.proyecto_atackdeck_android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_atackdeck_android.R

class AvatarAdapter(
    private val avatarList: List<String>,
    private val onAvatarSelected: (String) -> Unit
) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

    class AvatarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAvatarOption: ImageView = view.findViewById(R.id.imgAvatarOption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_avatar, parent, false)
        return AvatarViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        val avatarName = avatarList[position]


        val context = holder.itemView.context
        val resourceId = context.resources.getIdentifier(avatarName, "drawable", context.packageName)

        if (resourceId != 0) {
            holder.imgAvatarOption.setImageResource(resourceId)
        }

        // Al hacer clic, enviamos el nombre del avatar a la Actividad
        holder.itemView.setOnClickListener {
            onAvatarSelected(avatarName)
        }
    }

    override fun getItemCount(): Int = avatarList.size
}