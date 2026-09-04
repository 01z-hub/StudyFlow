package com.zyb.studyflow.data.repository

import com.zyb.studyflow.data.model.Section
import com.zyb.studyflow.data.model.StudyProgress
import com.zyb.studyflow.data.model.Textbook
import kotlinx.coroutines.flow.Flow

interface StudyRepository {
    suspend fun getTextbooks(): List<Textbook>

    fun observeProgress(): Flow<List<StudyProgress>>

    suspend fun startOrContinue(sectionId: String)

    suspend fun saveAnswer(
        sectionId: String,
        questionId: String,
        optionIndex: Int,
        currentQuestionIndex: Int,
    )

    suspend fun submit(section: Section)

    suspend fun completeReview(sectionId: String)
}
