package com.raxeltemematics.demoapp

data class TripPointModel (
    val latitude: Double = 0.toDouble(),
    val longitude: Double = 0.toDouble(),
    val speedColor: Int = 0,
    val alertTypeImage: Int = 0,
    val usePhone: Boolean = false
)