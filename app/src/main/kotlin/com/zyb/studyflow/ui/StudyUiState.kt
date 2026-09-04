package com.zyb.studyflow.ui

import com.zyb.studyflow.data.model.StudyProgress
import com.zyb.studyflow.data.model.Textbook

sealed interface StudyUiState {
    data object Loading : StudyUiState

    data object Empty : StudyUiState

    data class Error(
        val message: String,
    ) : StudyUiState

    data class Content(
        val textbooks: List<Textbook>,
        val progressBySection: Map<String, StudyProgress>,
        val actionMessage: String? = null,
    ) : StudyUiState
}
