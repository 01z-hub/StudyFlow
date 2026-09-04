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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.zyb.studyflow.data.model.ExerciseStatus
import com.zyb.studyflow.data.model.StudyProgress
import com.zyb.studyflow.data.model.Textbook
import com.zyb.studyflow.data.model.findSection

@Composable
fun LibraryScreen(
    textbooks: List<Textbook>,
    progressBySection: Map<String, StudyProgress>,
    onTextbookClick: (String) -> Unit,
    onContinue: (String) -> Unit,
) {
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    val activeProgress = progressBySection.values
        .filter { it.status == ExerciseStatus.IN_PROGRESS || it.status == ExerciseStatus.SELF_CHECKING }
        .maxByOrNull { it.updatedAt }
    val activeLocation = activeProgress?.let { textbooks.findSection(it.sectionId) }
    val allSections = textbooks.flatMap { book ->
        book.editions.flatMap { edition -> edition.chapters.flatMap { it.sections } }
    }
    val completedCount = progressBySection.values.count { it.status == ExerciseStatus.COMPLETED }
    val completion = if (allSections.isEmpty()) 0f else completedCount.toFloat() / allSections.size
    val visibleBooks = textbooks.filter {
        selectedSubject == null || it.subject == selectedSubject
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .pageWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "StudyFlow",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "今天，从一小节开始",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "所有教材、练习与进度都保存在本机。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    ProgressOverviewCard(
                        completed = completedCount,
                        total = allSections.size,
                        progress = completion,
                    )
                }

                if (activeProgress != null && activeLocation != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ContinueCard(
                            subject = activeLocation.textbook.subject,
                            sectionTitle = activeLocation.section.title,
                            status = activeProgress.status,
                            onClick = { onContinue(activeProgress.sectionId) },
                        )
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "我的教材",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(end = 16.dp),
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedSubject == null,
                                    onClick = { selectedSubject = null },
                                    label = { Text("全部") },
                                )
                            }
                            items(textbooks.map(Textbook::subject).distinct()) { subject ->
                                FilterChip(
                                    selected = selectedSubject == subject,
                                    onClick = { selectedSubject = subject },
                                    label = { Text(subject) },
                                )
                            }
                        }
                    }
                }

                items(
                    items = visibleBooks,
                    key = Textbook::id,
                ) { textbook ->
                    TextbookCard(
                        textbook = textbook,
                        completedSections = textbook.editions
                            .flatMap { it.chapters }
                            .flatMap { it.sections }
                            .count { progressBySection[it.id]?.status == ExerciseStatus.COMPLETED },
                        onClick = { onTextbookClick(textbook.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressOverviewCard(
    completed: Int,
    total: Int,
    progress: Float,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text("总学习进度", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "$completed / $total 小节",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.24f),
            )
        }
    }
}

@Composable
private fun ContinueCard(
    subject: String,
    sectionTitle: String,
    status: ExerciseStatus,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = if (status == ExerciseStatus.SELF_CHECKING) "待完成自批" else "继续上次学习",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "$subject · $sectionTitle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Button(onClick = onClick) {
                Text(if (status == ExerciseStatus.SELF_CHECKING) "去自批" else "继续")
            }
        }
    }
}

@Composable
private fun TextbookCard(
    textbook: Textbook,
    completedSections: Int,
    onClick: () -> Unit,
) {
    val accent = remember(textbook.accent) {
        runCatching { Color(textbook.accent.toColorInt()) }.getOrDefault(Color(0xFF3155E7))
    }
    val sectionCount = textbook.editions.flatMap { it.chapters }.sumOf { it.sections.size }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = textbook.subject.take(1),
                        color = accent,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                ) {
                    Text(
                        text = textbook.subject,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${textbook.grade} · ${textbook.editions.size} 个版本",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "$completedSections/$sectionCount",
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = textbook.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "选择教材版本  →",
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
