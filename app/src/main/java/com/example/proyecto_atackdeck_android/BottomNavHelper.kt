package com.example.proyecto_atackdeck_android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_atackdeck_android.MainActivity
import com.example.proyecto_atackdeck_android.MetaPokemonActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.proyecto_atackdeck_android.R
import com.example.proyecto_atackdeck_android.SearchActivity

object BottomNavHelper {
    fun setupBottomNav(activity: AppCompatActivity, bottomNavigationView: BottomNavigationView, currentItemId: Int) {

        // 1. Iluminar el ícono de la pantalla actual
        bottomNavigationView.selectedItemId = currentItemId

        // 2. Escuchar los clics
        bottomNavigationView.setOnItemSelectedListener { item ->
            // Si el usuario toca el botón de la pantalla en la que ya está, no hacemos nada
            if (item.itemId == currentItemId) return@setOnItemSelectedListener true

            when (item.itemId) {

                R.id.nav_home -> {
                    val intent = Intent(activity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT // Evita apilar pantallas
                    activity.startActivity(intent)
                    activity.overridePendingTransition(0, 0)
                    true
                }

                R.id.nav_search-> {
                    val intent = Intent(activity, SearchActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    activity.startActivity(intent)
                    activity.overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_user_deck -> {
                    val intent = Intent(activity, UserDeckActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    activity.startActivity(intent)
                    activity.overridePendingTransition(0, 0)
                    true
                }

                else -> false
            }
        }
    }
}