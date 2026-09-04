package com.zyb.studyflow.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zyb.studyflow.data.model.ExerciseStatus
import com.zyb.studyflow.data.model.Section
import com.zyb.studyflow.data.model.findSection
import com.zyb.studyflow.ui.screen.ChapterListScreen
import com.zyb.studyflow.ui.screen.EditionSelectionScreen
import com.zyb.studyflow.ui.screen.ExerciseScreen
import com.zyb.studyflow.ui.screen.LibraryScreen
import com.zyb.studyflow.ui.screen.ReportScreen

private const val LIBRARY_ROUTE = "library"
private const val EDITIONS_ROUTE = "editions/{bookId}"
private const val CHAPTERS_ROUTE = "chapters/{bookId}/{editionId}"
private const val EXERCISE_ROUTE = "exercise/{bookId}/{editionId}/{sectionId}"
private const val REPORT_ROUTE = "report/{bookId}/{editionId}/{sectionId}"

@Composable
fun StudyNavHost(
    state: StudyUiState.Content,
    onStartOrContinue: (String) -> Unit,
    onSaveAnswer: (String, String, Int, Int) -> Unit,
    onSubmit: (Section) -> Unit,
    onCompleteReview: (String) -> Unit,
) {
    val navController = rememberNavController()

    fun exerciseRoute(bookId: String, editionId: String, sectionId: String) =
        "exercise/$bookId/$editionId/$sectionId"

    fun reportRoute(bookId: String, editionId: String, sectionId: String) =
        "report/$bookId/$editionId/$sectionId"

    NavHost(
        navController = navController,
        startDestination = LIBRARY_ROUTE,
    ) {
        composable(LIBRARY_ROUTE) {
            LibraryScreen(
                textbooks = state.textbooks,
                progressBySection = state.progressBySection,
                onTextbookClick = { bookId ->
                    navController.navigate("editions/$bookId")
                },
                onContinue = { sectionId ->
                    state.textbooks.findSection(sectionId)?.let { location ->
                        val progress = state.progressBySection[sectionId]
                        if (progress?.status == ExerciseStatus.SELF_CHECKING ||
                            progress?.status == ExerciseStatus.COMPLETED
                        ) {
                            navController.navigate(
                                reportRoute(location.bookId, location.editionId, sectionId),
                            )
                        } else {
                            onStartOrContinue(sectionId)
                            navController.navigate(
                                exerciseRoute(location.bookId, location.editionId, sectionId),
                            )
                        }
                    }
                },
            )
        }

        composable(
            route = EDITIONS_ROUTE,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")
            val textbook = state.textbooks.firstOrNull { it.id == bookId }
            EditionSelectionScreen(
                textbook = textbook,
                progressBySection = state.progressBySection,
                onBack = navController::popBackStack,
                onEditionClick = { editionId ->
                    navController.navigate("chapters/$bookId/$editionId")
                },
            )
        }

        composable(
            route = CHAPTERS_ROUTE,
            arguments = studyArguments(),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")
            val editionId = backStackEntry.arguments?.getString("editionId")
            val textbook = state.textbooks.firstOrNull { it.id == bookId }
            val edition = textbook?.editions?.firstOrNull { it.id == editionId }
            ChapterListScreen(
                textbook = textbook,
                edition = edition,
                progressBySection = state.progressBySection,
                onBack = navController::popBackStack,
                onSectionClick = { section ->
                    val status = state.progressBySection[section.id]?.status
                        ?: ExerciseStatus.NOT_STARTED
                    if (status == ExerciseStatus.SELF_CHECKING ||
                        status == ExerciseStatus.COMPLETED
                    ) {
                        navController.navigate(reportRoute(bookId.orEmpty(), editionId.orEmpty(), section.id))
                    } else {
                        onStartOrContinue(section.id)
                        navController.navigate(
                            exerciseRoute(bookId.orEmpty(), editionId.orEmpty(), section.id),
                        )
                    }
                },
            )
        }

        composable(
            route = EXERCISE_ROUTE,
            arguments = studyArguments(includeSection = true),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId").orEmpty()
            val editionId = backStackEntry.arguments?.getString("editionId").orEmpty()
            val sectionId = backStackEntry.arguments?.getString("sectionId").orEmpty()
            val location = state.textbooks.findSection(sectionId)
            ExerciseScreen(
                section = location?.section,
                subject = location?.textbook?.subject.orEmpty(),
                progress = state.progressBySection[sectionId],
                onBack = navController::popBackStack,
                onAnswer = { questionId, optionIndex, questionIndex ->
                    onSaveAnswer(sectionId, questionId, optionIndex, questionIndex)
                },
                onSubmit = {
                    location?.section?.let(onSubmit)
                    navController.navigate(reportRoute(bookId, editionId, sectionId)) {
                        popUpTo(exerciseRoute(bookId, editionId, sectionId)) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(
            route = REPORT_ROUTE,
            arguments = studyArguments(includeSection = true),
        ) { backStackEntry ->
            val sectionId = backStackEntry.arguments?.getString("sectionId").orEmpty()
            val location = state.textbooks.findSection(sectionId)
            val progress = state.progressBySection[sectionId]
            ReportScreen(
                section = location?.section,
                subject = location?.textbook?.subject.orEmpty(),
                progress = progress,
                onBack = navController::popBackStack,
                onComplete = {
                    onCompleteReview(sectionId)
                    navController.popBackStack(
                        route = "chapters/${location?.bookId}/${location?.editionId}",
                        inclusive = false,
                    )
                },
            )
        }
    }
}

private fun studyArguments(includeSection: Boolean = false) = buildList {
    add(navArgument("bookId") { type = NavType.StringType })
    add(navArgument("editionId") { type = NavType.StringType })
    if (includeSection) {
        add(navArgument("sectionId") { type = NavType.StringType })
    }
}
