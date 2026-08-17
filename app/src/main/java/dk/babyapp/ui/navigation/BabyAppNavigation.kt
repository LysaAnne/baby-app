package dk.babyapp.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dk.babyapp.R
import dk.babyapp.ui.screen.PlaceholderScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabyAppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                AppDestination.topLevel.forEach { destination ->
                    val selected = currentDestination?.hasRoute(destination::class) == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination) {
                                popUpTo(AppDestination.Today) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(destination.labelRes)) },
                    )
                }
            }
        },
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Today,
        ) {
            composable<AppDestination.Today> {
                PlaceholderScreen(
                    title = stringResource(R.string.today_title),
                    description = stringResource(R.string.today_description),
                    contentPadding = contentPadding,
                )
            }
            composable<AppDestination.Timeline> {
                PlaceholderScreen(
                    title = stringResource(R.string.timeline_title),
                    description = stringResource(R.string.timeline_description),
                    contentPadding = contentPadding,
                )
            }
            composable<AppDestination.Insights> {
                PlaceholderScreen(
                    title = stringResource(R.string.insights_title),
                    description = stringResource(R.string.insights_description),
                    contentPadding = contentPadding,
                )
            }
            composable<AppDestination.Guide> {
                PlaceholderScreen(
                    title = stringResource(R.string.guide_title),
                    description = stringResource(R.string.guide_description),
                    contentPadding = contentPadding,
                )
            }
            composable<AppDestination.Family> {
                PlaceholderScreen(
                    title = stringResource(R.string.family_title),
                    description = stringResource(R.string.family_description),
                    contentPadding = contentPadding,
                )
            }
        }
    }
}

