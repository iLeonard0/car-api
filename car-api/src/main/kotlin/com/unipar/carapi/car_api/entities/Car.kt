package com.unipar.carapi.car_api.entities

data class Car(
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: Place
)

data class Place(
    val lat: Double,
    val long: Double
)