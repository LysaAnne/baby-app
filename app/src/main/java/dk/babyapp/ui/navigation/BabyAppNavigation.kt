package dk.babyapp.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dk.babyapp.R
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.ui.family.FamilyScreen
import dk.babyapp.ui.family.SettingsDialog
import dk.babyapp.ui.profile.ProfileDraft
import dk.babyapp.ui.profile.ProfileValidationError
import dk.babyapp.data.preferences.AppPreferences
import dk.babyapp.ui.OnboardingSettings
import dk.babyapp.data.profile.ParentProfile
import dk.babyapp.data.profile.ChildParentLink
import dk.babyapp.data.profile.CareProvider
import android.net.Uri
import java.io.File
import dk.babyapp.ui.screen.PlaceholderScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabyAppNavigation(
    profiles: List<ChildProfile>,
    activeChild: ChildProfile?,
    onSelectChild: (String) -> Unit,
    onSaveProfile: (ProfileDraft, (ProfileValidationError?) -> Unit) -> Unit,
    onDeleteProfile: (ChildProfile) -> Unit,
    photoFile: (String?) -> File?,
    onPhotoSelected: suspend (Uri) -> String,
    preferences: AppPreferences = AppPreferences(),
    onUpdateSettings: (OnboardingSettings) -> Unit = {},
    parents: List<ParentProfile> = emptyList(),
    parentLinks: List<ChildParentLink> = emptyList(),
    onSaveParent: (ParentProfile, Set<String>) -> Unit = { _, _ -> },
    onDeleteParent: (ParentProfile) -> Unit = {},
    careProviders: List<CareProvider> = emptyList(),
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    var settingsOpen by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            var childMenuOpen by remember { mutableStateOf(false) }
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Column {
                        Text(text = stringResource(R.string.app_name), style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                        Text(text = activeChild?.name ?: stringResource(R.string.no_active_child), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    }
                },
                actions = {
                    IconButton(onClick = { settingsOpen = true }) { Icon(Icons.Outlined.Settings, stringResource(R.string.settings)) }
                    IconButton(onClick = { childMenuOpen = true }) {
                        Icon(Icons.Outlined.ExpandMore, contentDescription = stringResource(R.string.switch_child))
                    }
                    DropdownMenu(expanded = childMenuOpen, onDismissRequest = { childMenuOpen = false }) {
                        profiles.forEach { profile ->
                            DropdownMenuItem(
                                text = { Text("${profile.avatar.symbol} ${profile.name}") },
                                onClick = { onSelectChild(profile.id); childMenuOpen = false },
                            )
                        }
                    }
                },
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
                if (activeChild == null) {
                    androidx.compose.foundation.layout.Column(
                        modifier = androidx.compose.ui.Modifier.padding(contentPadding).padding(24.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                    ) {
                        Text(stringResource(R.string.no_child_registered), style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
                        androidx.compose.material3.Button(onClick = { navController.navigate(AppDestination.Family) }) { Text(stringResource(R.string.go_to_family)) }
                    }
                } else PlaceholderScreen(
                    title = stringResource(R.string.today_title), description = stringResource(R.string.today_description), contentPadding = contentPadding,
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
                FamilyScreen(
                    profiles = profiles,
                    activeChildId = activeChild?.id,
                    contentPadding = contentPadding,
                    onSelectChild = onSelectChild,
                    onSaveProfile = onSaveProfile,
                    onDeleteProfile = onDeleteProfile,
                    photoFile = photoFile,
                    onPhotoSelected = onPhotoSelected,
                    preferences = preferences,
                    onUpdateSettings = onUpdateSettings,
                    parents = parents,
                    parentLinks = parentLinks,
                    onSaveParent = onSaveParent,
                    onDeleteParent = onDeleteParent,
                    careProviders = careProviders,
                )
            }
        }
    }
    if (settingsOpen) SettingsDialog(preferences, onUpdateSettings) { settingsOpen = false }
}
