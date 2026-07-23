package com.zyb.studyflow.ui

import com.zyb.studyflow.data.model.Chapter
import com.zyb.studyflow.data.model.Section
import com.zyb.studyflow.data.model.Textbook
import com.zyb.studyflow.data.model.TextbookEdition
import com.zyb.studyflow.data.repository.TextbookRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `successful load exposes content`() = runTest {
        val textbooks = listOf(sampleTextbook())
        val viewModel = CatalogViewModel(
            repository = TextbookRepository { textbooks },
        )

        advanceUntilIdle()

        assertEquals(CatalogUiState.Content(textbooks), viewModel.uiState.value)
    }

    @Test
    fun `empty repository exposes empty state`() = runTest {
        val viewModel = CatalogViewModel(
            repository = TextbookRepository { emptyList() },
        )

        advanceUntilIdle()

        assertSame(CatalogUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `repository failure exposes retryable error`() = runTest {
        var shouldFail = true
        val textbooks = listOf(sampleTextbook())
        val viewModel = CatalogViewModel(
            repository = TextbookRepository {
                if (shouldFail) error("test failure") else textbooks
            },
        )

        advanceUntilIdle()
        assertEquals(
            CatalogUiState.Error("教材加载失败，请稍后重试。"),
            viewModel.uiState.value,
        )

        shouldFail = false
        viewModel.refresh()
        advanceUntilIdle()
        assertEquals(CatalogUiState.Content(textbooks), viewModel.uiState.value)
    }

    private fun sampleTextbook() = Textbook(
        id = "math-7",
        subject = "数学",
        grade = "七年级",
        description = "测试教材",
        accent = "#4F46E5",
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
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )
}
