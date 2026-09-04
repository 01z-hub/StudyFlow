package com.zyb.studyflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zyb.studyflow.ui.StudyViewModel
import com.zyb.studyflow.ui.StudyFlowApp
import com.zyb.studyflow.ui.theme.StudyFlowTheme
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyFlowTheme {
                val studyViewModel: StudyViewModel = koinViewModel()
                StudyFlowApp(viewModel = studyViewModel)
            }
        }
    }
}
