package com.zyb.studyflow.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zyb.studyflow.data.model.ExerciseQuestion
import com.zyb.studyflow.data.model.Section
import com.zyb.studyflow.data.model.StudyProgress

@Composable
fun ExerciseScreen(
    section: Section?,
    subject: String,
    progress: StudyProgress?,
    onBack: () -> Unit,
    onAnswer: (questionId: String, optionIndex: Int, questionIndex: Int) -> Unit,
    onSubmit: () -> Unit,
) {
    if (section == null || section.questions.isEmpty()) {
        Scaffold(
            topBar = {
                PageHeader(
                    title = section?.title ?: "练习",
                    eyebrow = subject.ifBlank { null },
                    onBack = onBack,
                )
            },
        ) { padding ->
            MissingContent(
                modifier = Modifier.padding(padding),
                message = "这个小节暂时没有本地练习。",
                onBack = onBack,
            )
        }
        return
    }

    var questionIndex by rememberSaveable(section.id) {
        mutableIntStateOf(progress?.currentQuestionIndex?.coerceIn(section.questions.indices) ?: 0)
    }
    var answers by remember(section.id) {
        mutableStateOf(progress?.answers.orEmpty())
    }
    LaunchedEffect(progress?.answers) {
        if (progress != null) {
            answers = progress.answers
        }
    }

    val question = section.questions[questionIndex]
    val answeredCount = section.questions.count { it.id in answers }
    val allAnswered = answeredCount == section.questions.size

    Scaffold(
        topBar = {
            PageHeader(
                title = section.title,
                eyebrow = "$subject · 本地练习",
                onBack = onBack,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "第 ${questionIndex + 1} / ${section.questions.size} 题",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "已答 $answeredCount 题",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        LinearProgressIndicator(
                            progress = {
                                (questionIndex + 1).toFloat() / section.questions.size
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                item {
                    QuestionCard(
                        question = question,
                        selectedOption = answers[question.id],
                        onSelect = { optionIndex ->
                            answers = answers + (question.id to optionIndex)
                            onAnswer(question.id, optionIndex, questionIndex)
                        },
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                persistPosition(
                                    section = section,
                                    answers = answers,
                                    sourceIndex = questionIndex,
                                    targetIndex = questionIndex - 1,
                                    onAnswer = onAnswer,
                                )
                                questionIndex -= 1
                            },
                            enabled = questionIndex > 0,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("上一题")
                        }

                        if (questionIndex < section.questions.lastIndex) {
                            FilledTonalButton(
                                onClick = {
                                    persistPosition(
                                        section = section,
                                        answers = answers,
                                        sourceIndex = questionIndex,
                                        targetIndex = questionIndex + 1,
                                        onAnswer = onAnswer,
                                    )
                                    questionIndex += 1
                                },
                                enabled = answers[question.id] != null,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("下一题")
                            }
                        } else {
                            Button(
                                onClick = onSubmit,
                                enabled = allAnswered,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("提交并查看报告")
                            }
                        }
                    }
                }

                if (!allAnswered && questionIndex == section.questions.lastIndex) {
                    item {
                        Text(
                            text = "还剩 ${section.questions.size - answeredCount} 题未作答，可以返回补充。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(
    question: ExerciseQuestion,
    selectedOption: Int?,
    onSelect: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = question.prompt,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            question.options.forEachIndexed { index, option ->
                val selected = selectedOption == index
                Card(
                    onClick = { onSelect(index) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
                    border = if (selected) {
                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        null
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = { onSelect(index) },
                        )
                        Text(
                            text = option,
                            modifier = Modifier.padding(start = 6.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

private fun persistPosition(
    section: Section,
    answers: Map<String, Int>,
    sourceIndex: Int,
    targetIndex: Int,
    onAnswer: (String, Int, Int) -> Unit,
) {
    val sourceQuestion = section.questions[sourceIndex]
    val selectedOption = answers[sourceQuestion.id] ?: return
    onAnswer(sourceQuestion.id, selectedOption, targetIndex)
}
