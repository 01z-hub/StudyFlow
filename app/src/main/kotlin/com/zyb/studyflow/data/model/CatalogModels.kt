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
    val questions: List<ExerciseQuestion>,
)

data class ExerciseQuestion(
    val id: String,
    val prompt: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String,
)

data class SectionLocation(
    val bookId: String,
    val editionId: String,
    val textbook: Textbook,
    val edition: TextbookEdition,
    val section: Section,
)

fun List<Textbook>.findSection(sectionId: String): SectionLocation? {
    for (textbook in this) {
        for (edition in textbook.editions) {
            val section = edition.chapters
                .flatMap(Chapter::sections)
                .firstOrNull { it.id == sectionId }
            if (section != null) {
                return SectionLocation(
                    bookId = textbook.id,
                    editionId = edition.id,
                    textbook = textbook,
                    edition = edition,
                    section = section,
                )
            }
        }
    }
    return null
}
