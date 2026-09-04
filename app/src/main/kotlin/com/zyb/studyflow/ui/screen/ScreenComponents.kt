package com.zyb.studyflow.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zyb.studyflow.data.model.ExerciseStatus

internal val PageMaxWidth = 1080.dp

@Composable
internal fun PageHeader(
    title: String,
    eyebrow: String? = null,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack) {
            Text("返回")
        }
        Column(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            if (eyebrow != null) {
                Text(
                    text = eyebrow,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
internal fun MissingContent(
    modifier: Modifier = Modifier,
    message: String,
    onBack: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
        )
        TextButton(onClick = onBack) {
            Text("返回书架")
        }
    }
}

@Composable
internal fun StatusPill(
    status: ExerciseStatus,
    modifier: Modifier = Modifier,
) {
    val (container, content) = statusColors(status)
    Box(
        modifier = modifier
            .background(container, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = status.label(),
            style = MaterialTheme.typography.labelMedium,
            color = content,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun statusColors(status: ExerciseStatus): Pair<Color, Color> = when (status) {
    ExerciseStatus.NOT_STARTED -> MaterialTheme.colorScheme.surfaceVariant to
        MaterialTheme.colorScheme.onSurfaceVariant
    ExerciseStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer to
        MaterialTheme.colorScheme.onPrimaryContainer
    ExerciseStatus.SELF_CHECKING -> Color(0xFFFFE7C2) to Color(0xFF7A4100)
    ExerciseStatus.COMPLETED -> Color(0xFFD9F7E8) to Color(0xFF075E3C)
}

internal fun ExerciseStatus.label(): String = when (this) {
    ExerciseStatus.NOT_STARTED -> "未开始"
    ExerciseStatus.IN_PROGRESS -> "进行中"
    ExerciseStatus.SELF_CHECKING -> "自批中"
    ExerciseStatus.COMPLETED -> "已完成"
}

internal fun ExerciseStatus.actionLabel(): String = when (this) {
    ExerciseStatus.NOT_STARTED -> "开始练习"
    ExerciseStatus.IN_PROGRESS -> "继续学习"
    ExerciseStatus.SELF_CHECKING -> "继续自批"
    ExerciseStatus.COMPLETED -> "查看报告"
}

internal fun Modifier.pageWidth(): Modifier = widthIn(max = PageMaxWidth)
