package com.zyb.studyflow.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zyb.studyflow.data.model.ExerciseQuestion
import com.zyb.studyflow.data.model.ExerciseStatus
import com.zyb.studyflow.data.model.Section
import com.zyb.studyflow.data.model.StudyProgress

@Composable
fun ReportScreen(
    section: Section?,
    subject: String,
    progress: StudyProgress?,
    onBack: () -> Unit,
    onComplete: () -> Unit,
) {
    Scaffold(
        topBar = {
            PageHeader(
                title = section?.title ?: "练习报告",
                eyebrow = "$subject · 本地报告",
                onBack = onBack,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (section == null) {
            MissingContent(
                modifier = Modifier.padding(padding),
                message = "没有找到这份练习报告。",
                onBack = onBack,
            )
        } else {
            val answers = progress?.answers.orEmpty()
            val score = progress?.score ?: section.questions.count {
                answers[it.id] == it.correctOptionIndex
            }
            val total = section.questions.size
            val status = progress?.status ?: ExerciseStatus.IN_PROGRESS

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.TopCenter,
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .pageWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        ScoreCard(
                            score = score,
                            total = total,
                            status = status,
                        )
                    }
                    item {
                        Text(
                            text = "答题解析",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    itemsIndexed(
                        items = section.questions,
                        key = { _, question -> question.id },
                    ) { index, question ->
                        AnswerReviewCard(
                            index = index + 1,
                            question = question,
                            selectedOption = answers[question.id],
                        )
                    }
                    item {
                        if (status == ExerciseStatus.SELF_CHECKING) {
                            Button(
                                onClick = onComplete,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("完成自批")
                            }
                        } else if (status == ExerciseStatus.COMPLETED) {
                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("返回章节")
                            }
                        } else {
                            Text(
                                text = "正在保存报告，请稍候…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreCard(
    score: Int,
    total: Int,
    status: ExerciseStatus,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (status == ExerciseStatus.COMPLETED) "练习已完成" else "进入自批阶段",
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = "本地即时报告",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "$score/$total",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun AnswerReviewCard(
    index: Int,
    question: ExerciseQuestion,
    selectedOption: Int?,
) {
    val isCorrect = selectedOption == question.correctOptionIndex
    val resultColor = if (isCorrect) Color(0xFF087A50) else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "第 $index 题",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (isCorrect) "回答正确" else "需要订正",
                    style = MaterialTheme.typography.labelLarge,
                    color = resultColor,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = question.prompt,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "你的答案：${selectedOption?.let { question.options[it] } ?: "未作答"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = resultColor,
                )
                Text(
                    text = "正确答案：${question.options[question.correctOptionIndex]}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = question.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
