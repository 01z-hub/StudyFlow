package com.zyb.studyflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zyb.studyflow.data.repository.AssetTextbookRepository
import com.zyb.studyflow.ui.CatalogViewModel
import com.zyb.studyflow.ui.StudyFlowApp
import com.zyb.studyflow.ui.theme.StudyFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModelFactory = CatalogViewModel.Factory(
            repository = AssetTextbookRepository(applicationContext),
        )

        setContent {
            StudyFlowTheme {
                val catalogViewModel: CatalogViewModel = viewModel(
                    factory = viewModelFactory,
                )
                StudyFlowApp(viewModel = catalogViewModel)
            }
        }
    }
}
