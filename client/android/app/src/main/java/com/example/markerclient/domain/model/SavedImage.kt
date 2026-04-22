package com.example.markerclient.domain.model

data class SavedImage(
    val imagePath: String,
    val thumbnailPath: String?,
    val width: Int,
    val height: Int
)