package dk.babyapp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.babyapp.ui.navigation.BabyAppNavigation
import dk.babyapp.ui.onboarding.OnboardingScreen
import dk.babyapp.ui.theme.BabyAppTheme

@Composable
fun BabyApp(
    viewModel: AppViewModel = viewModel(),
    tracking: dk.babyapp.ui.tracking.TrackingViewModel = viewModel(),
    family: dk.babyapp.ui.family.FamilyViewModel = viewModel(),
    settings: dk.babyapp.ui.settings.SettingsViewModel = viewModel(),
    book: dk.babyapp.ui.book.BookViewModel = viewModel(),
    developer: dk.babyapp.ui.developer.DeveloperViewModel = viewModel(),
    bookTarget: dk.babyapp.reminders.BookNotificationTarget? = null, onBookTargetHandled: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(state.loaded, state.profiles, state.preferences.bookRemindersEnabled, state.preferences.bookReminderFrequency) { if (state.loaded) dk.babyapp.reminders.BookReminderScheduler(context).update(state.profiles, state.preferences.bookRemindersEnabled, state.preferences.bookReminderFrequency) }
    val activeTimer = state.unfinishedEvents.firstOrNull()
    LaunchedEffect(activeTimer?.id) { viewModel.restoreTimerNotification(activeTimer) }
    LaunchedEffect(state.loaded, state.preferences.languageTag) {
        if (state.loaded && AppCompatDelegate.getApplicationLocales().toLanguageTags() != "da") {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("da"))
        }
    }
    LaunchedEffect(state.loaded, state.preferences.dailyReminderEnabled, state.preferences.dailyReminderHour, state.preferences.dailyReminderMinute) {
        if (state.loaded) viewModel.syncDailyReminder()
    }
    val activeColorProfile = state.activeChild?.colorTheme?.let { id -> state.colorProfiles.firstOrNull { it.id == id } }
        ?: state.colorProfiles.firstOrNull()
    // Only a dark colour profile the user made gives dark bars; the app is otherwise always light.
    val shownDark = activeColorProfile?.isDark ?: false
    val view = androidx.compose.ui.platform.LocalView.current
    androidx.compose.runtime.SideEffect {
        (view.context as? android.app.Activity)?.window?.let { window ->
            androidx.core.view.WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !shownDark
                isAppearanceLightNavigationBars = !shownDark
            }
        }
    }
    BabyAppTheme(childColorTheme = activeColorProfile) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when {
                !state.loaded -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                !state.preferences.onboardingCompleted -> OnboardingScreen(viewModel, state.preferences)
                else -> BabyAppNavigation(
                    bookTarget = bookTarget, onBookTargetHandled = onBookTargetHandled,
                    profiles = state.profiles,
                    activeChild = state.activeChild,
                    onSelectChild = viewModel::selectChild,
                    onSaveProfile = family::saveProfile,
                    onUpdateChild = family::updateChild,
                    onDeleteProfile = family::deleteProfile,
                    photoFile = family::photoFile,
                    onPhotoSelected = family::importPhoto,
                    preferences = state.preferences,
                    onUpdateSettings = settings::updateSettings,
                    parents = state.parents,
                    parentLinks = state.parentLinks,
                    onSaveParent = family::saveFamilyMember,
                    onDeleteParent = family::deleteParent,
                    onReorderChildren = family::reorderChildren,
                    onReorderFamily = family::reorderFamily,
                    careProviders = state.careProviders,
                    careEvents = state.recentEvents,
                    runningEvents = state.unfinishedEvents,
                    recentFrom = state.recentFrom,
                    childHistory = { viewModel.childHistory.collectAsState().value ?: state.recentEvents },
                    journal = { viewModel.journal.collectAsState().value ?: JournalSlice(state.recentEvents, hasOlder = false) },
                    onLoadOlderJournal = viewModel::loadOlderJournal,
                    onRestoreCareEvent = tracking::restoreCareEvent,
                    onUpdateInsightCards = settings::updateInsightCards,
                    onUpdateInsightPeriod = settings::updateInsightPeriod,
                    onUpdateInsightRange = settings::updateInsightRange,
                    onUpdateSimpleQuickRegistration = settings::updateSimpleQuickRegistration,
                    onUpdateShowBulletinBoard = settings::updateShowBulletinBoard,
                    onUpdateShowDevelopmentStrip = settings::updateShowDevelopmentStrip,
                    onUpdateCustomReminders = settings::updateCustomReminders,
                    onUpdateDevelopmentReminders = settings::updateDevelopmentReminders,
                    onUpdateCollapsedCards = settings::updateCollapsedCards,
                    onUpdateTodayCardOrder = settings::updateTodayCardOrder,
                    onBackupSaved = settings::markBackupSaved,
                    onUpdateHealthQuestions = settings::updateHealthQuestions,
                    onUpdateBoardNotes = settings::updateBoardNotes,
                    onUpdateDevelopmentChecks = settings::updateDevelopmentChecks,
                    onUpdateSongbook = settings::updateSongbook,
                    onUpdateHealthReminders = settings::updateHealthReminders,
                    bookPages = state.bookPages,
                    onSaveBookPage = book::saveBookPage,
                    onDeleteBookPage = book::deleteCustomPage,
                    colorProfiles = state.colorProfiles,
                    onStartBreastfeeding = tracking::startBreastfeeding,
                    onResumeBreastfeeding = tracking::resumeBreastfeeding,
                    onStartPumping = tracking::startPumping,
                    onStartSleep = tracking::startSleep,
                    onStartActivity = tracking::startActivity,
                    onToggleTimer = tracking::toggleTimer,
                    onSwitchSide = tracking::switchBreastSide,
                    onStopTimer = tracking::stopTimer,
                    onAddBottle = tracking::addBottle,
                    onAddDiaper = tracking::addDiaper,
                    onAddManualTimer = tracking::addManualTimer,
                    onAddSleep = tracking::addSleep,
                    onAddMeasurement = tracking::addMeasurement,
                    onAddActivity = tracking::addActivity,
                    onSaveHealthRecord = { tracking.saveHealthRecord(it) },
                    onUpdateCareEvent = tracking::updateCareEvent,
                    onDeleteCareEvent = tracking::deleteCareEvent,
                    onUpdateQuickActions = settings::updateQuickActions,
                    onUpdateDashboardMetrics = settings::updateDashboardMetrics,
                    onUpdateQuickActionCategoryOrder = settings::updateQuickActionCategoryOrder,
                    onUpdateDefaultNippleShield = tracking::updateDefaultNippleShield,
                    onUpdateHeadSideStatistics = settings::updateHeadSideStatistics,
                    onUpdateMeasurementChartSettings = settings::updateMeasurementChartSettings,
                    onUpdateJournalQuickFilters = settings::updateJournalQuickFilters,
                    onUpdateHiddenQuickActions = settings::updateHiddenQuickActions,
                    onUpdateMedicines = tracking::updateMedicines,
                    medicines = state.medicines,
                    teeth = state.teeth,
                    onSaveTooth = tracking::saveTooth,
                    onUpdateBookReminderFrequency = book::updateBookReminderFrequency,
                    onUpdateBookReminders = book::updateBookReminders,
                    onUpdateDailyReminder = settings::updateDailyReminder,
                    createEncryptedBackup = settings::createEncryptedBackup,
                    restoreEncryptedBackup = settings::restoreEncryptedBackup,
                    onCreateDeveloperTestFamily = developer::createDeveloperTestFamily,
                    onCreateDeveloperPaletteChildren = developer::createDeveloperPaletteChildren,
                    onSaveColorProfile = { settings.saveColorProfile(it) },
                    onDeleteColorProfile = settings::deleteColorProfile,
                    onMoveColorProfile = settings::moveColorProfile,
                    onDismissGettingStarted = viewModel::dismissGettingStarted,
                )
            }
        }
    }
}
