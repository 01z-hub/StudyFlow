package com.zyb.studyflow.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun StudyFlowApp(
    viewModel: StudyViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        StudyUiState.Loading -> CenteredMessage {
            CircularProgressIndicator()
        }

        StudyUiState.Empty -> CenteredMessage {
            Text(
                text = "书架还是空的",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        is StudyUiState.Error -> CenteredMessage {
            Text(
                text = state.message,
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = viewModel::refresh) {
                Text("重新加载")
            }
        }

        is StudyUiState.Content -> {
            val snackbarHostState = remember { SnackbarHostState() }
            LaunchedEffect(state.actionMessage) {
                state.actionMessage?.let {
                    snackbarHostState.showSnackbar(it)
                    viewModel.consumeActionMessage()
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                StudyNavHost(
                    state = state,
                    onStartOrContinue = viewModel::startOrContinue,
                    onSaveAnswer = viewModel::saveAnswer,
                    onSubmit = viewModel::submit,
                    onCompleteReview = viewModel::completeReview,
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun CenteredMessage(
    content: @Composable () -> Unit,
) {
    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            ) {
                content()
            }
        }
    }
}
