package com.zyb.studyflow.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyProgressDao {
    @Query("SELECT * FROM study_progress ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<StudyProgressEntity>>

    @Query("SELECT * FROM study_progress WHERE section_id = :sectionId")
    suspend fun getBySectionId(sectionId: String): StudyProgressEntity?

    @Upsert
    suspend fun upsert(progress: StudyProgressEntity)
}
