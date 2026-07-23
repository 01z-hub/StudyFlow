package com.zyb.studyflow.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zyb.studyflow.data.model.Textbook
import com.zyb.studyflow.ui.screen.ChapterListScreen
import com.zyb.studyflow.ui.screen.EditionSelectionScreen
import com.zyb.studyflow.ui.screen.LibraryScreen

private const val LIBRARY_ROUTE = "library"
private const val EDITIONS_ROUTE = "editions/{bookId}"
private const val CHAPTERS_ROUTE = "chapters/{bookId}/{editionId}"

@Composable
fun CatalogNavHost(
    textbooks: List<Textbook>,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LIBRARY_ROUTE,
    ) {
        composable(LIBRARY_ROUTE) {
            LibraryScreen(
                textbooks = textbooks,
                onTextbookClick = { bookId ->
                    navController.navigate("editions/$bookId")
                },
            )
        }

        composable(
            route = EDITIONS_ROUTE,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")
            val textbook = textbooks.firstOrNull { it.id == bookId }
            EditionSelectionScreen(
                textbook = textbook,
                onBack = navController::popBackStack,
                onEditionClick = { editionId ->
                    navController.navigate("chapters/$bookId/$editionId")
                },
            )
        }

        composable(
            route = CHAPTERS_ROUTE,
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("editionId") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")
            val editionId = backStackEntry.arguments?.getString("editionId")
            val textbook = textbooks.firstOrNull { it.id == bookId }
            val edition = textbook?.editions?.firstOrNull { it.id == editionId }
            ChapterListScreen(
                textbook = textbook,
                edition = edition,
                onBack = navController::popBackStack,
            )
        }
    }
}
