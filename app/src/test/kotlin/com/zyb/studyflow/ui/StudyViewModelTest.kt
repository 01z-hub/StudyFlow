package com.zyb.studyflow.ui

import com.zyb.studyflow.data.model.Chapter
import com.zyb.studyflow.data.model.ExerciseStatus
import com.zyb.studyflow.data.model.Section
import com.zyb.studyflow.data.model.StudyProgress
import com.zyb.studyflow.data.model.Textbook
import com.zyb.studyflow.data.model.TextbookEdition
import com.zyb.studyflow.data.repository.StudyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `successful load combines catalog and persisted progress`() = runTest {
        val progress = StudyProgress(
            sectionId = "section-1",
            status = ExerciseStatus.IN_PROGRESS,
            currentQuestionIndex = 0,
            answers = emptyMap(),
            score = null,
            totalQuestions = null,
            updatedAt = 1L,
        )
        val repository = FakeStudyRepository(
            textbooks = listOf(sampleTextbook()),
            initialProgress = listOf(progress),
        )

        val viewModel = StudyViewModel(repository)
        advanceUntilIdle()

        assertEquals(
            StudyUiState.Content(
                textbooks = listOf(sampleTextbook()),
                progressBySection = mapOf("section-1" to progress),
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `empty catalog exposes empty state`() = runTest {
        val viewModel = StudyViewModel(FakeStudyRepository(textbooks = emptyList()))

        advanceUntilIdle()

        assertSame(StudyUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `catalog failure exposes retryable error`() = runTest {
        val repository = FakeStudyRepository(
            textbooks = listOf(sampleTextbook()),
            shouldFail = true,
        )
        val viewModel = StudyViewModel(repository)

        advanceUntilIdle()
        assertEquals(
            StudyUiState.Error("离线教材加载失败，请检查本地数据后重试。"),
            viewModel.uiState.value,
        )

        repository.shouldFail = false
        viewModel.refresh()
        advanceUntilIdle()
        assertEquals(
            StudyUiState.Content(
                textbooks = listOf(sampleTextbook()),
                progressBySection = emptyMap(),
            ),
            viewModel.uiState.value,
        )
    }

    private class FakeStudyRepository(
        private val textbooks: List<Textbook>,
        initialProgress: List<StudyProgress> = emptyList(),
        var shouldFail: Boolean = false,
    ) : StudyRepository {
        private val progress = MutableStateFlow(initialProgress)

        override suspend fun getTextbooks(): List<Textbook> {
            if (shouldFail) error("test failure")
            return textbooks
        }

        override fun observeProgress(): Flow<List<StudyProgress>> = progress

        override suspend fun startOrContinue(sectionId: String) = Unit

        override suspend fun saveAnswer(
            sectionId: String,
            questionId: String,
            optionIndex: Int,
            currentQuestionIndex: Int,
        ) = Unit

        override suspend fun submit(section: Section) = Unit

        override suspend fun completeReview(sectionId: String) = Unit
    }
}

private fun sampleTextbook() = Textbook(
    id = "math-7",
    subject = "数学",
    grade = "七年级",
    description = "测试教材",
    accent = "#3155E7",
    editions = listOf(
        TextbookEdition(
            id = "math-basic",
            name = "基础版",
            year = "2026",
            description = "测试版本",
            chapters = listOf(
                Chapter(
                    id = "chapter-1",
                    title = "第一章",
                    sections = listOf(
                        Section(
                            id = "section-1",
                            title = "第一节",
                            durationMinutes = 20,
                            questions = emptyList(),
                        ),
                    ),
                ),
            ),
        ),
    ),
)
