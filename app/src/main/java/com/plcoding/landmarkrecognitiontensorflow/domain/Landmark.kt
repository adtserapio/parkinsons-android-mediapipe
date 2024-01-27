package com.plcoding.landmarkrecognitiontensorflow.domain

data class Landmark(
    val name: String,
    val coordinates: List<Int>
)
