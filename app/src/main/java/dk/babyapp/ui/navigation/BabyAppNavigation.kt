package dk.babyapp.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
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
import dk.babyapp.data.color.ColorProfile
import android.net.Uri
import java.io.File
import dk.babyapp.ui.screen.PlaceholderScreen
import dk.babyapp.data.tracking.BottleContent
import dk.babyapp.data.tracking.BreastSide
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.ui.tracking.TodayScreen
import dk.babyapp.ui.tracking.TimelineScreen
import dk.babyapp.data.tracking.SleepQuality
import dk.babyapp.data.tracking.SleepType
import java.time.LocalDate
import dk.babyapp.domain.shouldShowDueDateReminder

internal fun shouldReturnToTodayAfterProfileSave(
    profilesWereEmpty: Boolean,
    draftId: String?,
    error: ProfileValidationError?,
): Boolean = profilesWereEmpty && draftId == null && error == null

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
    onReorderChildren: (List<String>) -> Unit = {},
    onReorderFamily: (List<String>) -> Unit = {},
    careProviders: List<CareProvider> = emptyList(),
    careEvents: List<CareEventEntity> = emptyList(),
    colorProfiles: List<ColorProfile> = emptyList(),
    onStartBreastfeeding: (String, BreastSide) -> Unit = { _, _ -> },
    onStartPumping: (String) -> Unit = {},
    onStartSleep: (String, SleepType, (Boolean) -> Unit) -> Unit = { _, _, result -> result(false) },
    onToggleTimer: (CareEventEntity) -> Unit = {},
    onSwitchSide: (CareEventEntity) -> Unit = {},
    onStopTimer: (CareEventEntity, Int?, (CareEventEntity) -> Unit) -> Unit = { _, _, _ -> },
    onAddBottle: (String, Long, BottleContent, Int?, Int?, String) -> Unit = { _, _, _, _, _, _ -> },
    onAddDiaper: (String, Long, DiaperType, String, String, (CareEventEntity) -> Unit) -> Unit = { _, _, _, _, _, _ -> },
    onAddManualTimer: (String, CareEventType, Long, Long, BreastSide?, Int?, String) -> Unit = { _, _, _, _, _, _, _ -> },
    onAddSleep: (String, Long, Long, SleepType, String, String, Int?, SleepQuality?, String, (Boolean) -> Unit) -> Unit = { _, _, _, _, _, _, _, _, _, result -> result(false) },
    onSaveHealthRecord: (CareEventEntity) -> Unit = {},
    onUpdateCareEvent: (CareEventEntity, (Boolean) -> Unit) -> Unit = { _, result -> result(true) },
    onDeleteCareEvent: (CareEventEntity) -> Unit = {},
    onUpdateQuickActions: (Boolean, Boolean, Boolean, Boolean) -> Unit = { _, _, _, _ -> },
    onCreateDeveloperTestFamily: (() -> Unit) -> Unit = { it() },
    onCreateDeveloperPaletteChildren: (() -> Unit) -> Unit = { it() },
    onSaveColorProfile: (ColorProfile) -> Unit = {},
    onDeleteColorProfile: (String, (Boolean) -> Unit) -> Unit = { _, done -> done(false) },
    onMoveColorProfile: (String, Int) -> Unit = { _, _ -> },
    exportColorProfiles: () -> String = { "[]" },
    importColorProfiles: (String, (Boolean) -> Unit) -> Unit = { _, done -> done(false) },
    onDismissGettingStarted: () -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    var settingsOpen by remember { mutableStateOf(false) }
    var requestedEditChildId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            var childMenuOpen by remember { mutableStateOf(false) }
            TopAppBar(
                title = {
                    Box {
                        TextButton(
                            onClick = { childMenuOpen = true },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                                contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        ) {
                            Column {
                                Text(
                                    text = activeChild?.let { "${it.avatar.symbol} ${it.name}" } ?: stringResource(R.string.no_active_child),
                                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                )
                                if (activeChild != null) Text(
                                    "Du registrerer for ${activeChild.name}",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                )
                            }
                            Icon(Icons.Outlined.ExpandMore, contentDescription = stringResource(R.string.switch_child))
                        }
                        DropdownMenu(expanded = childMenuOpen, onDismissRequest = { childMenuOpen = false }) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("Vælg aktivt barn", style = androidx.compose.material3.MaterialTheme.typography.labelLarge) },
                                enabled = false,
                                onClick = {},
                            )
                            profiles.forEach { profile ->
                                DropdownMenuItem(
                                    text = { Text("${profile.avatar.symbol} ${profile.name}") },
                                    onClick = { onSelectChild(profile.id); childMenuOpen = false },
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { settingsOpen = true }) { Icon(Icons.Outlined.Settings, stringResource(R.string.settings)) }
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
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
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
                TodayScreen(
                    childId = activeChild?.id, events = careEvents, contentPadding = contentPadding, preferences = preferences,
                    careProviders = activeChild?.let { child -> careProviders.filter { it.childId == child.id } }.orEmpty(),
                    overdueDueDate = activeChild?.dueDate?.takeIf { shouldShowDueDateReminder(activeChild.birthStatus, it, LocalDate.now()) },
                    onOpenFamily = { activeChild?.let { requestedEditChildId = it.id }; navController.navigate(AppDestination.Family) },
                    onStartBreastfeeding = onStartBreastfeeding, onStartPumping = onStartPumping, onStartSleep = onStartSleep,
                    onToggleTimer = onToggleTimer, onSwitchSide = onSwitchSide, onStopTimer = onStopTimer,
                    onAddBottle = onAddBottle, onAddDiaper = onAddDiaper, onAddManualTimer = onAddManualTimer, onAddSleep = onAddSleep,
                    onUpdate = onUpdateCareEvent, onDelete = onDeleteCareEvent,
                    onUpdateQuickActions = onUpdateQuickActions,
                    onOpenTimeline = { navController.navigate(AppDestination.Timeline) },
                    onSaveHealthRecord = onSaveHealthRecord,
                )
                if (activeChild == null && !preferences.hasSeenGettingStarted) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = onDismissGettingStarted,
                        title = { Text(stringResource(R.string.getting_started_title)) },
                        text = { Text(stringResource(R.string.getting_started_message)) },
                        confirmButton = {
                            androidx.compose.material3.Button(onClick = { onDismissGettingStarted(); navController.navigate(AppDestination.Family) }) {
                                Text(stringResource(R.string.create_first_child))
                            }
                        },
                        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismissGettingStarted) { Text(stringResource(R.string.close)) } },
                    )
                }
            }
            composable<AppDestination.Timeline> {
                TimelineScreen(
                    activeChildId = activeChild?.id,
                    events = careEvents,
                    careProviders = activeChild?.let { child -> careProviders.filter { it.childId == child.id } }.orEmpty(),
                    contentPadding = contentPadding,
                    onAddSleep = onAddSleep,
                    onAddBottle = onAddBottle,
                    onAddDiaper = onAddDiaper,
                    onAddManualTimer = onAddManualTimer,
                    onUpdate = onUpdateCareEvent,
                    onDelete = onDeleteCareEvent,
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
                    onSaveProfile = { draft, onResult ->
                        val profilesWereEmpty = profiles.isEmpty()
                        onSaveProfile(draft) { error ->
                            onResult(error)
                            if (shouldReturnToTodayAfterProfileSave(profilesWereEmpty, draft.id, error)) {
                                navController.navigate(AppDestination.Today) {
                                    popUpTo(AppDestination.Today) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    onDeleteProfile = onDeleteProfile,
                    photoFile = photoFile,
                    onPhotoSelected = onPhotoSelected,
                    preferences = preferences,
                    onUpdateSettings = onUpdateSettings,
                    parents = parents,
                    parentLinks = parentLinks,
                    onSaveParent = onSaveParent,
                    onDeleteParent = onDeleteParent,
                    onReorderChildren = onReorderChildren,
                    onReorderFamily = onReorderFamily,
                    careProviders = careProviders,
                    colorProfiles = colorProfiles,
                    requestedEditChildId = requestedEditChildId,
                    onEditRequestHandled = { requestedEditChildId = null },
                )
            }
        }
    }
    if (settingsOpen) SettingsDialog(
        preferences = preferences,
        onUpdate = onUpdateSettings,
        onCreateDeveloperTestFamily = onCreateDeveloperTestFamily,
        onCreateDeveloperPaletteChildren = onCreateDeveloperPaletteChildren,
        colorProfiles = colorProfiles,
        usedColorProfileIds = profiles.map { it.colorTheme }.toSet(),
        onSaveColorProfile = onSaveColorProfile,
        onDeleteColorProfile = onDeleteColorProfile,
        onMoveColorProfile = onMoveColorProfile,
        exportColorProfiles = exportColorProfiles,
        importColorProfiles = importColorProfiles,
        onDismiss = { settingsOpen = false },
    )
}
