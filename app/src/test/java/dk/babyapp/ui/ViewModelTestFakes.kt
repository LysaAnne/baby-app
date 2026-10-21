package dk.babyapp.ui

import android.net.Uri
import dk.babyapp.data.preferences.AppPreferences
import dk.babyapp.data.preferences.AppPreferencesRepository
import dk.babyapp.data.preferences.DanishRegion
import dk.babyapp.data.preferences.MeasurementUnits
import dk.babyapp.data.preferences.ThemePreference
import dk.babyapp.data.color.ColorProfile
import dk.babyapp.data.color.ColorProfileRepository
import dk.babyapp.ui.theme.defaultColorProfiles
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.profile.ChildColorTheme
import dk.babyapp.data.profile.ChildProfileRepository
import dk.babyapp.data.profile.ProfileImageStorage
import dk.babyapp.data.profile.ParentProfile
import dk.babyapp.data.profile.ParentProfileRepository
import dk.babyapp.data.profile.ChildParentLink
import dk.babyapp.data.profile.CareProvider
import dk.babyapp.data.profile.BiologicalSex
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventRepository
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.MeasurementType
import dk.babyapp.data.tracking.SleepType
import dk.babyapp.tracking.TimerNotificationController
import dk.babyapp.ui.profile.ProfileDraft
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class FakeProfilesRepository(initial: List<ChildProfile> = emptyList()) : ChildProfileRepository {
    val items = MutableStateFlow(initial)
    override val profiles: Flow<List<ChildProfile>> = items
    override val careProviders = MutableStateFlow<List<CareProvider>>(emptyList())
    override suspend fun get(id: String) = items.value.firstOrNull { it.id == id }
    override suspend fun save(profile: ChildProfile) {
        items.value = items.value.filterNot { it.id == profile.id } + profile
    }
    override suspend fun delete(profile: ChildProfile) {
        items.value = items.value.filterNot { it.id == profile.id }
    }
    override suspend fun setCareProviders(childId: String, providers: List<CareProvider>) { careProviders.value = providers.map { it.copy(childId = childId) } }
    override suspend fun setOrder(ids: List<String>) {
        val positions = ids.withIndex().associate { it.value to it.index }
        items.value = items.value.sortedBy { positions[it.id] ?: Int.MAX_VALUE }
    }
}

internal class FakePreferencesRepository(initial: AppPreferences = AppPreferences()) : AppPreferencesRepository {
    override suspend fun updateHeadSideStatistics(settings: dk.babyapp.data.preferences.HeadSideStatisticsSettings) { items.value = items.value.copy(headSideStatistics = settings) }
    override suspend fun updateMeasurementChartSettings(settings: dk.babyapp.data.preferences.MeasurementChartSettings) { items.value = items.value.copy(measurementChartSettings = settings) }
    override suspend fun updateLastPumpingMethod(method: String) { items.value = items.value.copy(lastPumpingMethod = method) }
    val items = MutableStateFlow(initial)
    override val preferences: Flow<AppPreferences> = items
    override suspend fun updateOnboarding(
        languageTag: String,
        region: DanishRegion,
        units: MeasurementUnits,
        theme: ThemePreference,
        activeChildId: String?,
    ) {
        items.value = AppPreferences(true, activeChildId, languageTag, region, units, theme)
    }
    override suspend fun setActiveChild(id: String?) { items.value = items.value.copy(activeChildId = id) }
    override suspend fun setTheme(theme: ThemePreference) { items.value = items.value.copy(theme = theme) }
    override suspend fun updateSettings(
        languageTag: String,
        region: DanishRegion,
        units: MeasurementUnits,
        theme: ThemePreference,
    ) { items.value = items.value.copy(languageTag = languageTag, region = region, units = units, theme = theme) }
    override suspend fun updateQuickActions(showBreastfeeding: Boolean, showBottle: Boolean, showPumping: Boolean, showDiaper: Boolean) {
        items.value = items.value.copy(showBreastfeedingQuickAction = showBreastfeeding, showBottleQuickAction = showBottle, showPumpingQuickAction = showPumping, showDiaperQuickAction = showDiaper)
    }
    override suspend fun updateDashboardMetrics(metrics: List<dk.babyapp.data.preferences.DashboardMetric>) {
        items.value = items.value.copy(dashboardMetrics = metrics)
    }
    override suspend fun updateBookReminderFrequency(frequency: dk.babyapp.data.preferences.BookReminderFrequency) { items.value = items.value.copy(bookReminderFrequency = frequency) }
    override suspend fun updateBookReminders(enabled: Boolean) { items.value = items.value.copy(bookRemindersEnabled = enabled) }
    override suspend fun updateDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
        items.value = items.value.copy(dailyReminderEnabled = enabled, dailyReminderHour = hour, dailyReminderMinute = minute)
    }
    override suspend fun updateInsightDashboardMetrics(metrics: List<String>) { items.value = items.value.copy(insightDashboardMetrics = metrics) }
    override suspend fun updateQuickActionCategoryOrder(order: List<dk.babyapp.data.preferences.QuickActionCategory>) { items.value = items.value.copy(quickActionCategoryOrder = order) }
    override suspend fun updateDefaultNippleShield(enabled: Boolean) { items.value = items.value.copy(defaultNippleShield = enabled) }
    override suspend fun updateJournalQuickFilters(filters: List<String>) { items.value = items.value.copy(journalQuickFilters = filters) }
    override suspend fun updateHiddenQuickActions(hidden: Set<String>) { items.value = items.value.copy(hiddenQuickActions = hidden) }
    override suspend fun updateInsightCards(cards: List<dk.babyapp.data.preferences.InsightCard>) { items.value = items.value.copy(insightCards = cards) }
    override suspend fun updateInsightPeriod(days: Int) { items.value = items.value.copy(insightPeriodDays = days) }
    override suspend fun markGettingStartedSeen() { items.value = items.value.copy(hasSeenGettingStarted = true) }
}

internal class FakePhotoStorage : ProfileImageStorage {
    override fun import(uri: Uri) = "photo"
    override fun file(fileName: String) = File(fileName)
    override fun delete(fileName: String) = Unit
}

internal class FakeParentRepository : ParentProfileRepository {
    override val parents = MutableStateFlow<List<ParentProfile>>(emptyList())
    override val links = MutableStateFlow<List<ChildParentLink>>(emptyList())
    override suspend fun save(parent: ParentProfile) { parents.value = parents.value.filterNot { it.id == parent.id } + parent }
    override suspend fun delete(parent: ParentProfile) { parents.value = parents.value.filterNot { it.id == parent.id } }
    override suspend fun setParents(childId: String, parentIds: Set<String>) {
        links.value = links.value.filterNot { it.childId == childId } + parentIds.map { ChildParentLink(childId, it) }
    }
    override suspend fun setChildren(memberId: String, childIds: Set<String>) {
        links.value = links.value.filterNot { it.parentId == memberId } + childIds.map { ChildParentLink(it, memberId) }
    }
    override suspend fun setOrder(ids: List<String>) {
        val positions = ids.withIndex().associate { it.value to it.index }
        parents.value = parents.value.sortedBy { positions[it.id] ?: Int.MAX_VALUE }
    }
}

internal class FakeCareEventRepository : CareEventRepository {
    val items = MutableStateFlow<List<CareEventEntity>>(emptyList())
    override fun eventsForChild(childId: String): Flow<List<CareEventEntity>> = items.map { all -> all.filter { it.childId == childId } }
    override fun recentEventsForChild(childId: String, from: Long): Flow<List<CareEventEntity>> =
        items.map { all -> all.filter { it.childId == childId && (it.startedAt >= from || it.endedAt == null || it.isDraft) } }
    override fun oldestStart(childId: String): Flow<Long?> = items.map { all -> all.filter { it.childId == childId && it.deletedAt == null }.minOfOrNull { it.startedAt } }
    override val unfinishedEvents: Flow<List<CareEventEntity>> = items.map { all -> all.filter { it.endedAt == null && it.deletedAt == null } }
    override suspend fun unfinished() = items.value.filter { it.endedAt == null && it.deletedAt == null }
    override suspend fun sleepsOverlapping(childId: String, start: Long, end: Long) =
        items.value.filter { it.childId == childId && it.type == CareEventType.Sleep && it.deletedAt == null && it.startedAt < end && (it.endedAt == null || it.endedAt > start) }
    override suspend fun save(event: CareEventEntity) { items.value = items.value.filterNot { it.id == event.id } + event }
    override suspend fun get(id: String) = items.value.firstOrNull { it.id == id }
    override suspend fun activeForChild(childId: String) = items.value.firstOrNull { it.childId == childId && it.endedAt == null && it.deletedAt == null }
    override suspend fun softDelete(event: CareEventEntity) { items.value = items.value.filterNot { it.id == event.id } }
}
internal class FakeTimerNotifications : TimerNotificationController { override fun show(eventId: String) = Unit; override fun hide() = Unit }

internal class FakeColorProfileRepository : ColorProfileRepository {
    private val items = MutableStateFlow(defaultColorProfiles)
    override val profiles: Flow<List<ColorProfile>> = items
    override suspend fun save(profile: ColorProfile) { items.value = items.value.filterNot { it.id == profile.id } + profile }
    override suspend fun delete(id: String) { items.value = items.value.filterNot { it.id == id } }
    override suspend fun replaceAll(profiles: List<ColorProfile>) { items.value = profiles }
    override suspend fun move(id: String, direction: Int) {
        val current = items.value.toMutableList()
        val from = current.indexOfFirst { it.id == id }
        if (from < 0) return
        val group = current.indices.filter { current[it].isDark == current[from].isDark }
        val toPosition = group.indexOf(from) + direction
        if (toPosition !in group.indices) return
        val to = group[toPosition]
        val item = current[from]; current[from] = current[to]; current[to] = item
        items.value = current
    }
}
