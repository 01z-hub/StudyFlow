package com.zyb.studyflow.data.repository

import android.content.Context
import com.zyb.studyflow.data.model.Chapter
import com.zyb.studyflow.data.model.Section
import com.zyb.studyflow.data.model.Textbook
import com.zyb.studyflow.data.model.TextbookEdition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AssetTextbookRepository(
    private val context: Context,
) : TextbookRepository {

    override suspend fun getTextbooks(): List<Textbook> = withContext(Dispatchers.IO) {
        val json = context.assets.open(CATALOG_ASSET).bufferedReader().use { it.readText() }
        parseCatalog(json)
    }

    private fun parseCatalog(json: String): List<Textbook> {
        val books = JSONObject(json).getJSONArray("books")
        return List(books.length()) { bookIndex ->
            val book = books.getJSONObject(bookIndex)
            Textbook(
                id = book.getString("id"),
                subject = book.getString("subject"),
                grade = book.getString("grade"),
                description = book.getString("description"),
                accent = book.getString("accent"),
                editions = book.getJSONArray("editions").let { editions ->
                    List(editions.length()) { editionIndex ->
                        val edition = editions.getJSONObject(editionIndex)
                        TextbookEdition(
                            id = edition.getString("id"),
                            name = edition.getString("name"),
                            year = edition.getString("year"),
                            description = edition.getString("description"),
                            chapters = edition.getJSONArray("chapters").let { chapters ->
                                List(chapters.length()) { chapterIndex ->
                                    val chapter = chapters.getJSONObject(chapterIndex)
                                    Chapter(
                                        id = chapter.getString("id"),
                                        title = chapter.getString("title"),
                                        sections = chapter.getJSONArray("sections").let { sections ->
                                            List(sections.length()) { sectionIndex ->
                                                val section = sections.getJSONObject(sectionIndex)
                                                Section(
                                                    id = section.getString("id"),
                                                    title = section.getString("title"),
                                                    durationMinutes = section.getInt("durationMinutes"),
                                                )
                                            }
                                        },
                                    )
                                }
                            },
                        )
                    }
                },
            )
        }
    }

    private companion object {
        const val CATALOG_ASSET = "catalog.json"
    }
}
