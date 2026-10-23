package com.example.proyecto_atackdeck_android.util

fun String.toNaturalFormat(): String {
    return this.replace('-', ' ').replaceFirstChar { it.titlecase() }
}