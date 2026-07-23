package com.zyb.studyflow.data.repository

import com.zyb.studyflow.data.model.Textbook

fun interface TextbookRepository {
    suspend fun getTextbooks(): List<Textbook>
}
