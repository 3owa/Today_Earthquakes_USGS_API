package com.usgs

data class Earthquake(
    val magnitude: Double,
    val place: String,
    val time: Long,
    val coordinates: List<Double>,
)