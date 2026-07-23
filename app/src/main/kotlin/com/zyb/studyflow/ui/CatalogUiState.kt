package com.zyb.studyflow.ui

import com.zyb.studyflow.data.model.Textbook

sealed interface CatalogUiState {
    data object Loading : CatalogUiState

    data object Empty : CatalogUiState

    data class Error(
        val message: String,
    ) : CatalogUiState

    data class Content(
        val textbooks: List<Textbook>,
    ) : CatalogUiState
}
