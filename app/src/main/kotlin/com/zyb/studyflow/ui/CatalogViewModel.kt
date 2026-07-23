package com.zyb.studyflow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zyb.studyflow.data.repository.TextbookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CatalogViewModel(
    private val repository: TextbookRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            _uiState.value = runCatching { repository.getTextbooks() }
                .fold(
                    onSuccess = { textbooks ->
                        if (textbooks.isEmpty()) {
                            CatalogUiState.Empty
                        } else {
                            CatalogUiState.Content(textbooks)
                        }
                    },
                    onFailure = {
                        CatalogUiState.Error(
                            message = "教材加载失败，请稍后重试。",
                        )
                    },
                )
        }
    }

    class Factory(
        private val repository: TextbookRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(CatalogViewModel::class.java))
            return CatalogViewModel(repository) as T
        }
    }
}
