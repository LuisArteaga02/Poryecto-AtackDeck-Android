package com.example.proyecto_atackdeck_android.util

fun String.toPokeFormat(): String {
    return this.split("-").joinToString(separator = " ") { word ->
        word.replaceFirstChar { it.titlecase() }
    }
}