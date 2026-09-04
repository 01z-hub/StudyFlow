package com.zyb.studyflow.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ExerciseWorkflowTest {

    @Test
    fun `exercise follows the only legal four-state path`() {
        val inProgress = ExerciseWorkflow.start(ExerciseStatus.NOT_STARTED)
        val selfChecking = ExerciseWorkflow.submit(inProgress)
        val completed = ExerciseWorkflow.completeReview(selfChecking)

        assertEquals(ExerciseStatus.IN_PROGRESS, inProgress)
        assertEquals(ExerciseStatus.SELF_CHECKING, selfChecking)
        assertEquals(ExerciseStatus.COMPLETED, completed)
    }

    @Test
    fun `completed exercise cannot be submitted again`() {
        assertThrows(IllegalArgumentException::class.java) {
            ExerciseWorkflow.submit(ExerciseStatus.COMPLETED)
        }
    }

    @Test
    fun `score is calculated from self-authored question answers`() {
        val section = Section(
            id = "section",
            title = "练习",
            durationMinutes = 10,
            questions = listOf(
                ExerciseQuestion("q1", "1+1=?", listOf("1", "2"), 1, "1+1=2"),
                ExerciseQuestion("q2", "2+2=?", listOf("3", "4"), 1, "2+2=4"),
            ),
        )

        val score = ExerciseScorer.score(
            section = section,
            answers = mapOf("q1" to 1, "q2" to 0),
        )

        assertEquals(1, score)
    }
}
