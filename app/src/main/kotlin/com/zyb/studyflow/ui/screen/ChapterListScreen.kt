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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zyb.studyflow.data.model.Chapter
import com.zyb.studyflow.data.model.ExerciseStatus
import com.zyb.studyflow.data.model.Section
import com.zyb.studyflow.data.model.StudyProgress
import com.zyb.studyflow.data.model.Textbook
import com.zyb.studyflow.data.model.TextbookEdition

private enum class SectionFilter(val title: String, val status: ExerciseStatus?) {
    ALL("全部", null),
    NOT_STARTED("未开始", ExerciseStatus.NOT_STARTED),
    IN_PROGRESS("进行中", ExerciseStatus.IN_PROGRESS),
    SELF_CHECKING("自批中", ExerciseStatus.SELF_CHECKING),
    COMPLETED("已完成", ExerciseStatus.COMPLETED),
}

@Composable
fun ChapterListScreen(
    textbook: Textbook?,
    edition: TextbookEdition?,
    progressBySection: Map<String, StudyProgress>,
    onBack: () -> Unit,
    onSectionClick: (Section) -> Unit,
) {
    var selectedFilter by remember { mutableStateOf(SectionFilter.ALL) }

    Scaffold(
        topBar = {
            PageHeader(
                title = edition?.name ?: "章节目录",
                eyebrow = textbook?.let { "${it.subject} · ${it.grade}" },
                onBack = onBack,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (textbook == null || edition == null) {
            MissingContent(
                modifier = Modifier.padding(padding),
                message = "没有找到这个教材版本。",
                onBack = onBack,
            )
        } else {
            val allSections = edition.chapters.flatMap(Chapter::sections)
            val completed = allSections.count {
                progressBySection[it.id]?.status == ExerciseStatus.COMPLETED
            }
            val progress = if (allSections.isEmpty()) 0f else completed.toFloat() / allSections.size
            val visibleChapters = edition.chapters.mapNotNull { chapter ->
                val visibleSections = chapter.sections.filter { section ->
                    selectedFilter.status == null ||
                        (progressBySection[section.id]?.status ?: ExerciseStatus.NOT_STARTED) ==
                        selectedFilter.status
                }
                chapter.takeIf { visibleSections.isNotEmpty() }?.copy(sections = visibleSections)
            }

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
                        EditionProgressCard(
                            completed = completed,
                            total = allSections.size,
                            progress = progress,
                        )
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(end = 16.dp),
                        ) {
                            items(SectionFilter.entries, key = SectionFilter::name) { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    label = { Text(filter.title) },
                                )
                            }
                        }
                    }
                    if (visibleChapters.isEmpty()) {
                        item {
                            Text(
                                text = "这个状态下还没有小节。",
                                modifier = Modifier.padding(vertical = 32.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    items(visibleChapters, key = Chapter::id) { chapter ->
                        ChapterCard(
                            chapter = chapter,
                            progressBySection = progressBySection,
                            onSectionClick = onSectionClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditionProgressCard(
    completed: Int,
    total: Int,
    progress: Float,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "本教材进度",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "$completed / $total",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: Chapter,
    progressBySection: Map<String, StudyProgress>,
    onSectionClick: (Section) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = chapter.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            chapter.sections.forEachIndexed { index, section ->
                val status = progressBySection[section.id]?.status ?: ExerciseStatus.NOT_STARTED
                SectionRow(
                    index = index + 1,
                    section = section,
                    status = status,
                    onClick = { onSectionClick(section) },
                )
            }
        }
    }
}

@Composable
private fun SectionRow(
    index: Int,
    section: Section,
    status: ExerciseStatus,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(10.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 7.dp),
            ) {
                Text(
                    text = index.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${section.durationMinutes} 分钟 · ${section.questions.size} 题",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusPill(status)
        }
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(status.actionLabel())
        }
    }
}
