package com.zyb.studyflow.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zyb.studyflow.data.local.StudyFlowDatabase
import com.zyb.studyflow.data.model.ExerciseQuestion
import com.zyb.studyflow.data.model.ExerciseStatus
import com.zyb.studyflow.data.model.Section
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineStudyRepositoryTest {

    private lateinit var database: StudyFlowDatabase
    private lateinit var repository: OfflineStudyRepository
    private var timestamp = 0L

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StudyFlowDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = OfflineStudyRepository(
            textbookRepository = TextbookRepository { emptyList() },
            progressDao = database.studyProgressDao(),
            now = { ++timestamp },
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun progressSurvivesEveryStepOfTheExerciseWorkflow() = runTest {
        val section = sampleSection()

        repository.startOrContinue(section.id)
        assertEquals(
            ExerciseStatus.IN_PROGRESS,
            repository.observeProgress().first().single().status,
        )

        repository.saveAnswer(section.id, "q1", 1, 1)
        repository.saveAnswer(section.id, "q2", 0, 1)
        repository.submit(section)

        val submitted = repository.observeProgress().first().single()
        assertEquals(ExerciseStatus.SELF_CHECKING, submitted.status)
        assertEquals(2, submitted.score)
        assertEquals(mapOf("q1" to 1, "q2" to 0), submitted.answers)

        repository.completeReview(section.id)

        val completed = repository.observeProgress().first().single()
        assertEquals(ExerciseStatus.COMPLETED, completed.status)
        assertEquals(2, completed.score)
    }

    private fun sampleSection() = Section(
        id = "section",
        title = "测试小节",
        durationMinutes = 10,
        questions = listOf(
            ExerciseQuestion(
                id = "q1",
                prompt = "1+1=?",
                options = listOf("1", "2"),
                correctOptionIndex = 1,
                explanation = "1+1=2",
            ),
            ExerciseQuestion(
                id = "q2",
                prompt = "2-1=?",
                options = listOf("1", "2"),
                correctOptionIndex = 0,
                explanation = "2-1=1",
            ),
        ),
    )
}
