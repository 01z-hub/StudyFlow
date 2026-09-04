package com.zyb.studyflow.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_progress")
data class StudyProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "section_id")
    val sectionId: String,
    val status: String,
    @ColumnInfo(name = "current_question_index")
    val currentQuestionIndex: Int,
    @ColumnInfo(name = "answers_json")
    val answersJson: String,
    val score: Int?,
    @ColumnInfo(name = "total_questions")
    val totalQuestions: Int?,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
