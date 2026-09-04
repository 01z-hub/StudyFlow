package com.zyb.studyflow.data.model

enum class ExerciseStatus {
    NOT_STARTED,
    IN_PROGRESS,
    SELF_CHECKING,
    COMPLETED,
}

data class StudyProgress(
    val sectionId: String,
    val status: ExerciseStatus,
    val currentQuestionIndex: Int,
    val answers: Map<String, Int>,
    val score: Int?,
    val totalQuestions: Int?,
    val updatedAt: Long,
)

object ExerciseWorkflow {
    fun start(current: ExerciseStatus): ExerciseStatus = when (current) {
        ExerciseStatus.NOT_STARTED -> ExerciseStatus.IN_PROGRESS
        ExerciseStatus.IN_PROGRESS,
        ExerciseStatus.SELF_CHECKING,
        ExerciseStatus.COMPLETED,
        -> current
    }

    fun submit(current: ExerciseStatus): ExerciseStatus {
        require(current == ExerciseStatus.IN_PROGRESS) {
            "只有进行中的练习可以提交"
        }
        return ExerciseStatus.SELF_CHECKING
    }

    fun completeReview(current: ExerciseStatus): ExerciseStatus {
        require(current == ExerciseStatus.SELF_CHECKING) {
            "只有自批中的练习可以完成"
        }
        return ExerciseStatus.COMPLETED
    }
}

object ExerciseScorer {
    fun score(
        section: Section,
        answers: Map<String, Int>,
    ): Int = section.questions.count { question ->
        answers[question.id] == question.correctOptionIndex
    }
}
