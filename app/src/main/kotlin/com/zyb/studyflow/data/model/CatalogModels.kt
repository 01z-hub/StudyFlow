package com.zyb.studyflow.data.model

data class Textbook(
    val id: String,
    val subject: String,
    val grade: String,
    val description: String,
    val accent: String,
    val editions: List<TextbookEdition>,
)

data class TextbookEdition(
    val id: String,
    val name: String,
    val year: String,
    val description: String,
    val chapters: List<Chapter>,
)

data class Chapter(
    val id: String,
    val title: String,
    val sections: List<Section>,
)

data class Section(
    val id: String,
    val title: String,
    val durationMinutes: Int,
)
