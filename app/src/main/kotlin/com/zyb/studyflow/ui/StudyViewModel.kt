package com.zyb.studyflow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zyb.studyflow.data.model.Section
import com.zyb.studyflow.data.model.StudyProgress
import com.zyb.studyflow.data.model.Textbook
import com.zyb.studyflow.data.repository.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudyViewModel(
    private val repository: StudyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudyUiState>(StudyUiState.Loading)
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    private var textbooks: List<Textbook>? = null
    private var progressBySection: Map<String, StudyProgress> = emptyMap()

    init {
        observeProgress()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = StudyUiState.Loading
            runCatching { repository.getTextbooks() }
                .onSuccess {
                    textbooks = it
                    publishContent()
                }
                .onFailure {
                    _uiState.value = StudyUiState.Error(
                        message = "离线教材加载失败，请检查本地数据后重试。",
                    )
                }
        }
    }

    fun startOrContinue(sectionId: String) {
        runAction { repository.startOrContinue(sectionId) }
    }

    fun saveAnswer(
        sectionId: String,
        questionId: String,
        optionIndex: Int,
        currentQuestionIndex: Int,
    ) {
        runAction {
            repository.saveAnswer(
                sectionId = sectionId,
                questionId = questionId,
                optionIndex = optionIndex,
                currentQuestionIndex = currentQuestionIndex,
            )
        }
    }

    fun submit(section: Section) {
        runAction { repository.submit(section) }
    }

    fun completeReview(sectionId: String) {
        runAction { repository.completeReview(sectionId) }
    }

    fun consumeActionMessage() {
        val current = _uiState.value
        if (current is StudyUiState.Content && current.actionMessage != null) {
            _uiState.value = current.copy(actionMessage = null)
        }
    }

    private fun observeProgress() {
        viewModelScope.launch {
            repository.observeProgress().collect { progress ->
                progressBySection = progress.associateBy(StudyProgress::sectionId)
                publishContent()
            }
        }
    }

    private fun runAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { action() }
                .onFailure { throwable ->
                    val current = _uiState.value
                    if (current is StudyUiState.Content) {
                        _uiState.value = current.copy(
                            actionMessage = throwable.message ?: "操作失败，请重试。",
                        )
                    }
                }
        }
    }

    private fun publishContent() {
        val currentTextbooks = textbooks ?: return
        _uiState.value = if (currentTextbooks.isEmpty()) {
            StudyUiState.Empty
        } else {
            StudyUiState.Content(
                textbooks = currentTextbooks,
                progressBySection = progressBySection,
            )
        }
    }
}
