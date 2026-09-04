package com.zyb.studyflow.data.repository

import com.zyb.studyflow.data.local.StudyProgressDao
import com.zyb.studyflow.data.local.StudyProgressEntity
import com.zyb.studyflow.data.model.ExerciseStatus
import com.zyb.studyflow.data.model.ExerciseScorer
import com.zyb.studyflow.data.model.ExerciseWorkflow
import com.zyb.studyflow.data.model.Section
import com.zyb.studyflow.data.model.StudyProgress
import com.zyb.studyflow.data.model.Textbook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

class OfflineStudyRepository(
    private val textbookRepository: TextbookRepository,
    private val progressDao: StudyProgressDao,
    private val now: () -> Long = System::currentTimeMillis,
) : StudyRepository {

    override suspend fun getTextbooks(): List<Textbook> = textbookRepository.getTextbooks()

    override fun observeProgress(): Flow<List<StudyProgress>> =
        progressDao.observeAll().map { rows -> rows.map(StudyProgressEntity::toModel) }

    override suspend fun startOrContinue(sectionId: String) {
        val current = progressDao.getBySectionId(sectionId)?.toModel()
            ?: StudyProgress(
                sectionId = sectionId,
                status = ExerciseStatus.NOT_STARTED,
                currentQuestionIndex = 0,
                answers = emptyMap(),
                score = null,
                totalQuestions = null,
                updatedAt = now(),
            )
        progressDao.upsert(
            current.copy(
                status = ExerciseWorkflow.start(current.status),
                updatedAt = now(),
            ).toEntity(),
        )
    }

    override suspend fun saveAnswer(
        sectionId: String,
        questionId: String,
        optionIndex: Int,
        currentQuestionIndex: Int,
    ) {
        val current = progressDao.getBySectionId(sectionId)?.toModel()
            ?: StudyProgress(
                sectionId = sectionId,
                status = ExerciseStatus.IN_PROGRESS,
                currentQuestionIndex = 0,
                answers = emptyMap(),
                score = null,
                totalQuestions = null,
                updatedAt = now(),
            )
        require(current.status == ExerciseStatus.IN_PROGRESS) {
            "只能修改进行中练习的答案"
        }
        progressDao.upsert(
            current.copy(
                currentQuestionIndex = currentQuestionIndex,
                answers = current.answers + (questionId to optionIndex),
                updatedAt = now(),
            ).toEntity(),
        )
    }

    override suspend fun submit(section: Section) {
        require(section.questions.isNotEmpty()) { "练习题不能为空" }
        val current = requireNotNull(progressDao.getBySectionId(section.id)?.toModel()) {
            "请先开始练习"
        }
        require(section.questions.all { it.id in current.answers }) {
            "请完成全部题目后再提交"
        }
        val score = ExerciseScorer.score(section, current.answers)
        progressDao.upsert(
            current.copy(
                status = ExerciseWorkflow.submit(current.status),
                score = score,
                totalQuestions = section.questions.size,
                currentQuestionIndex = section.questions.lastIndex,
                updatedAt = now(),
            ).toEntity(),
        )
    }

    override suspend fun completeReview(sectionId: String) {
        val current = requireNotNull(progressDao.getBySectionId(sectionId)?.toModel()) {
            "没有找到练习进度"
        }
        progressDao.upsert(
            current.copy(
                status = ExerciseWorkflow.completeReview(current.status),
                updatedAt = now(),
            ).toEntity(),
        )
    }
}

private fun StudyProgressEntity.toModel(): StudyProgress = StudyProgress(
    sectionId = sectionId,
    status = runCatching { ExerciseStatus.valueOf(status) }
        .getOrDefault(ExerciseStatus.NOT_STARTED),
    currentQuestionIndex = currentQuestionIndex,
    answers = JSONObject(answersJson).let { json ->
        buildMap {
            json.keys().forEach { key -> put(key, json.getInt(key)) }
        }
    },
    score = score,
    totalQuestions = totalQuestions,
    updatedAt = updatedAt,
)

private fun StudyProgress.toEntity(): StudyProgressEntity = StudyProgressEntity(
    sectionId = sectionId,
    status = status.name,
    currentQuestionIndex = currentQuestionIndex,
    answersJson = JSONObject(answers).toString(),
    score = score,
    totalQuestions = totalQuestions,
    updatedAt = updatedAt,
)
