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
fun BabyApp(viewModel: AppViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val activeTimer = state.activeChild?.let { child -> state.careEvents.firstOrNull { it.childId == child.id && it.endedAt == null } }
    LaunchedEffect(activeTimer?.id) { viewModel.restoreTimerNotification(activeTimer) }
    LaunchedEffect(state.loaded, state.preferences.languageTag) {
        if (state.loaded && AppCompatDelegate.getApplicationLocales().toLanguageTags() != "da") {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("da"))
        }
    }
    val activeColorProfile = state.activeChild?.colorTheme?.let { id -> state.colorProfiles.firstOrNull { it.id == id } }
        ?: state.colorProfiles.firstOrNull()
    BabyAppTheme(childColorTheme = activeColorProfile) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when {
                !state.loaded -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                !state.preferences.onboardingCompleted -> OnboardingScreen(viewModel, state.preferences)
                else -> BabyAppNavigation(
                    profiles = state.profiles,
                    activeChild = state.activeChild,
                    onSelectChild = viewModel::selectChild,
                    onSaveProfile = viewModel::saveProfile,
                    onDeleteProfile = viewModel::deleteProfile,
                    photoFile = viewModel::photoFile,
                    onPhotoSelected = viewModel::importPhoto,
                    preferences = state.preferences,
                    onUpdateSettings = viewModel::updateSettings,
                    parents = state.parents,
                    parentLinks = state.parentLinks,
                    onSaveParent = viewModel::saveFamilyMember,
                    onDeleteParent = viewModel::deleteParent,
                    onReorderChildren = viewModel::reorderChildren,
                    onReorderFamily = viewModel::reorderFamily,
                    careProviders = state.careProviders,
                    careEvents = state.careEvents,
                    colorProfiles = state.colorProfiles,
                    onStartBreastfeeding = viewModel::startBreastfeeding,
                    onStartPumping = viewModel::startPumping,
                    onStartSleep = viewModel::startSleep,
                    onToggleTimer = viewModel::toggleTimer,
                    onSwitchSide = viewModel::switchBreastSide,
                    onStopTimer = viewModel::stopTimer,
                    onAddBottle = viewModel::addBottle,
                    onAddDiaper = viewModel::addDiaper,
                    onAddManualTimer = viewModel::addManualTimer,
                    onAddSleep = viewModel::addSleep,
                    onSaveHealthRecord = { viewModel.saveHealthRecord(it) },
                    onUpdateCareEvent = viewModel::updateCareEvent,
                    onDeleteCareEvent = viewModel::deleteCareEvent,
                    onUpdateQuickActions = viewModel::updateQuickActions,
                    onCreateDeveloperTestFamily = viewModel::createDeveloperTestFamily,
                    onCreateDeveloperPaletteChildren = viewModel::createDeveloperPaletteChildren,
                    onSaveColorProfile = { viewModel.saveColorProfile(it) },
                    onDeleteColorProfile = viewModel::deleteColorProfile,
                    onMoveColorProfile = viewModel::moveColorProfile,
                    exportColorProfiles = viewModel::exportColorProfiles,
                    importColorProfiles = viewModel::importColorProfiles,
                    onDismissGettingStarted = viewModel::dismissGettingStarted,
                )
            }
        }
    }
}
