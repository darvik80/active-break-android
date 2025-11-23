package xyz.crearts.activebreak.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import xyz.crearts.activebreak.ui.screens.activities.ActivitiesScreen
import xyz.crearts.activebreak.ui.screens.home.HomeScreen
import xyz.crearts.activebreak.ui.screens.settings.SettingsScreen
import xyz.crearts.activebreak.ui.screens.todo.TodoScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Activities : Screen("activities")
    object Todo : Screen("todo")
}

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        composable(Screen.Activities.route) {
            ActivitiesScreen(navController = navController)
        }

        composable(Screen.Todo.route) {
            TodoScreen(navController = navController)
        }
    }
}
