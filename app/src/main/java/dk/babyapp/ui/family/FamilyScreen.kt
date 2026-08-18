package dk.babyapp.ui.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.R
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.profile.ParentProfile
import dk.babyapp.data.profile.ChildParentLink
import dk.babyapp.data.profile.FamilyMemberRole
import dk.babyapp.data.profile.CareProvider
import dk.babyapp.data.profile.CareProviderType
import dk.babyapp.data.color.ColorProfile
import dk.babyapp.data.preferences.AppPreferences
import dk.babyapp.data.preferences.MeasurementUnits
import dk.babyapp.data.preferences.DanishRegion
import dk.babyapp.ui.OnboardingSettings
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dk.babyapp.domain.calculateChildAge
import dk.babyapp.domain.totalMonths
import dk.babyapp.ui.profile.ProfileDraft
import dk.babyapp.ui.profile.ProfileForm
import dk.babyapp.ui.profile.ProfileValidationError
import dk.babyapp.ui.profile.toDraft
import dk.babyapp.ui.profile.messageRes
import dk.babyapp.ui.profile.labelRes
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.launch
import java.io.File
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import kotlin.math.abs
import android.app.ActivityManager
import dk.babyapp.BuildConfig
import dk.babyapp.ui.theme.ColorProfileManagerDialog

@Composable
fun FamilyScreen(
    profiles: List<ChildProfile>,
    activeChildId: String?,
    contentPadding: PaddingValues,
    onSaveProfile: (ProfileDraft, (ProfileValidationError?) -> Unit) -> Unit,
    onDeleteProfile: (ChildProfile) -> Unit,
    photoFile: (String?) -> File?,
    onPhotoSelected: suspend (Uri) -> String,
    preferences: AppPreferences,
    onUpdateSettings: (OnboardingSettings) -> Unit,
    parents: List<ParentProfile>,
    parentLinks: List<ChildParentLink>,
    onSaveParent: (ParentProfile, Set<String>) -> Unit,
    onDeleteParent: (ParentProfile) -> Unit,
    onReorderChildren: (List<String>) -> Unit,
    onReorderFamily: (List<String>) -> Unit,
    careProviders: List<CareProvider>,
    colorProfiles: List<ColorProfile>,
    requestedEditChildId: String? = null,
    onEditRequestHandled: () -> Unit = {},
) {
    var editing by remember { mutableStateOf<ChildProfile?>(null) }
    var viewing by remember { mutableStateOf<ChildProfile?>(null) }
    var adding by remember { mutableStateOf(false) }
    var addMenuOpen by remember { mutableStateOf(false) }
    var addingMemberRole by remember { mutableStateOf<FamilyMemberRole?>(null) }
    var editingMember by remember { mutableStateOf<ParentProfile?>(null) }
    var viewingMember by remember { mutableStateOf<ParentProfile?>(null) }
    var deleting by remember { mutableStateOf<ChildProfile?>(null) }
    var reviewingRelations by remember { mutableStateOf(false) }
    var reorderingChildren by remember { mutableStateOf(false) }
    var reorderingFamily by remember { mutableStateOf(false) }

    LaunchedEffect(requestedEditChildId, profiles) {
        requestedEditChildId?.let { id ->
            profiles.firstOrNull { it.id == id }?.let { profile ->
                editing = profile
                onEditRequestHandled()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.child_profiles), style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = { reorderingChildren = true }, enabled = profiles.size > 1) {
                        Icon(Icons.Outlined.DragHandle, stringResource(R.string.reorder_children))
                    }
                }
            }
            items(profiles, key = { it.id }) { profile ->
                ProfileCard(
                    profile = profile,
                    selected = profile.id == activeChildId,
                    onView = { viewing = profile },
                    onEdit = { editing = profile },
                    photoFile = photoFile(profile.photoFileName),
                )
            }
            if (profiles.isEmpty()) item { Text(stringResource(R.string.no_profiles)) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.family_members), style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { reorderingFamily = true }, enabled = parents.size > 1) {
                            Icon(Icons.Outlined.DragHandle, stringResource(R.string.reorder_family))
                        }
                    }
                    OutlinedButton(
                        onClick = { reviewingRelations = true },
                        enabled = profiles.isNotEmpty() && parents.isNotEmpty(),
                    ) { Text(stringResource(R.string.review_relations)) }
                }
            }
            items(parents, key = { it.id }) { member ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        val memberBitmap = photoFile(member.photoFileName)?.let { BitmapFactory.decodeFile(it.path)?.asImageBitmap() }
                        if (memberBitmap != null) Image(memberBitmap, null, Modifier.size(44.dp).clip(CircleShape), contentScale = ContentScale.Crop) else Text(member.avatar.symbol, style = MaterialTheme.typography.headlineMedium)
                        Column(Modifier.weight(1f)) { Text(member.name); Text(roleLabel(member.role), style = MaterialTheme.typography.bodySmall) }
                        IconButton(onClick = { viewingMember = member }) { Icon(Icons.Outlined.Visibility, "Se ${member.name}") }
                        IconButton(onClick = { editingMember = member }) { Icon(Icons.Outlined.Edit, "Rediger ${member.name}") }
                    }
                }
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)) {
            FloatingActionButton(onClick = { addMenuOpen = true }) { Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_profile)) }
            DropdownMenu(expanded = addMenuOpen, onDismissRequest = { addMenuOpen = false }) {
                DropdownMenuItem(text = { Text(stringResource(R.string.add_child)) }, onClick = { addMenuOpen = false; adding = true })
                DropdownMenuItem(text = { Text(stringResource(R.string.add_parent)) }, onClick = { addMenuOpen = false; addingMemberRole = FamilyMemberRole.ParentNotSpecified })
                DropdownMenuItem(text = { Text(stringResource(R.string.add_family_member)) }, onClick = { addMenuOpen = false; addingMemberRole = FamilyMemberRole.OtherNotSpecified })
            }
        }
    }

    viewing?.let { profile ->
        ProfileViewDialog(
            profile = profile,
            units = preferences.units,
            photoFile = photoFile(profile.photoFileName),
            parents = parents.filter { parent -> parentLinks.any { it.childId == profile.id && it.parentId == parent.id } },
            siblings = run {
                val sharedParentIds = parentLinks.filter { it.childId == profile.id }.map { it.parentId }.filter { id -> parents.any { it.id == id && it.role.isParent } }.toSet()
                profiles.filter { child -> child.id != profile.id && parentLinks.any { it.childId == child.id && it.parentId in sharedParentIds } }
            },
            familyPhotoFile = photoFile,
            providers = careProviders.filter { it.childId == profile.id },
            onEdit = { viewing = null; editing = profile },
            onDismiss = { viewing = null },
        )
    }

    if (adding || editing != null) {
        ProfileEditorDialog(
            existing = editing,
            onSaveProfile = onSaveProfile,
            photoFile = photoFile,
            onPhotoSelected = onPhotoSelected,
            units = preferences.units,
            onDelete = editing?.let { profile -> { adding = false; editing = null; deleting = profile } },
            parents = parents,
            linkedParentIds = editing?.let { child -> parentLinks.filter { it.childId == child.id }.map { it.parentId }.toSet() }.orEmpty(),
            onSaveParent = onSaveParent,
            onDeleteParent = onDeleteParent,
            children = profiles,
            familyLinks = parentLinks,
            careProviders = editing?.let { child -> careProviders.filter { it.childId == child.id } }.orEmpty(),
            colorProfiles = colorProfiles,
            onDismiss = { adding = false; editing = null },
        )
    }
    addingMemberRole?.let { role -> ParentEditorDialog(
        existing = null, initialRole = role, children = profiles, selectedChildIds = emptySet(),
        onSave = onSaveParent, onDelete = {}, photoFile = photoFile, onPhotoSelected = onPhotoSelected, onDismiss = { addingMemberRole = null },
    ) }
    editingMember?.let { member -> ParentEditorDialog(
        existing = member, initialRole = member.role, children = profiles,
        selectedChildIds = parentLinks.filter { it.parentId == member.id }.map { it.childId }.toSet(),
        onSave = onSaveParent, onDelete = { onDeleteParent(it); editingMember = null }, photoFile = photoFile, onPhotoSelected = onPhotoSelected, onDismiss = { editingMember = null },
    ) }
    viewingMember?.let { member ->
        AlertDialog(
            onDismissRequest = { viewingMember = null },
            title = { Text(member.name) },
            text = { FamilyMemberView(member, photoFile(member.photoFileName)) },
            confirmButton = { Button(onClick = { viewingMember = null; editingMember = member }) { Icon(Icons.Outlined.Edit, null); Text(stringResource(R.string.edit_parent)) } },
            dismissButton = { TextButton(onClick = { viewingMember = null }) { Text(stringResource(R.string.close)) } },
        )
    }
    if (reviewingRelations) RelationsDialog(
        children = profiles,
        members = parents,
        existingLinks = parentLinks,
        onSave = { links ->
            parents.forEach { member ->
                onSaveParent(member, links.filter { it.parentId == member.id }.map { it.childId }.toSet())
            }
            reviewingRelations = false
        },
        onDismiss = { reviewingRelations = false },
    )
    if (reorderingChildren) ReorderProfilesDialog(
        title = stringResource(R.string.reorder_children),
        items = profiles.map { Triple(it.id, it.name, it.avatar.symbol) },
        onSave = { onReorderChildren(it); reorderingChildren = false },
        onDismiss = { reorderingChildren = false },
    )
    if (reorderingFamily) ReorderProfilesDialog(
        title = stringResource(R.string.reorder_family),
        items = parents.map { Triple(it.id, it.name, it.avatar.symbol) },
        onSave = { onReorderFamily(it); reorderingFamily = false },
        onDismiss = { reorderingFamily = false },
    )
    deleting?.let { profile ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.delete_child_title, profile.name)) },
            text = { Text(stringResource(R.string.delete_child_message)) },
            confirmButton = {
                Button(onClick = { onDeleteProfile(profile); deleting = null }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
fun SettingsDialog(
    preferences: AppPreferences,
    onUpdate: (OnboardingSettings) -> Unit,
    onCreateDeveloperTestFamily: (() -> Unit) -> Unit,
    onCreateDeveloperPaletteChildren: (() -> Unit) -> Unit,
    colorProfiles: List<ColorProfile>,
    usedColorProfileIds: Set<String>,
    onSaveColorProfile: (ColorProfile) -> Unit,
    onDeleteColorProfile: (String, (Boolean) -> Unit) -> Unit,
    onMoveColorProfile: (String, Int) -> Unit,
    exportColorProfiles: () -> String,
    importColorProfiles: (String, (Boolean) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    var settings by remember(preferences) { mutableStateOf(OnboardingSettings(preferences.languageTag, preferences.region, preferences.units, preferences.theme)) }
    var confirmClearData by remember { mutableStateOf(false) }
    var testFamilyCreated by remember { mutableStateOf(false) }
    var creatingTestFamily by remember { mutableStateOf(false) }
    var paletteChildrenCreated by remember { mutableStateOf(false) }
    var creatingPaletteChildren by remember { mutableStateOf(false) }
    var palettePreviewOpen by remember { mutableStateOf(false) }
    var developerToolsOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    fun update(value: OnboardingSettings) { settings = value; onUpdate(value) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.units))
                MeasurementUnits.entries.forEach { units ->
                    FilterChip(settings.units == units, onClick = { update(settings.copy(units = units)) }, label = { Text(stringResource(if (units == MeasurementUnits.Metric) R.string.units_metric else R.string.units_imperial)) })
                }
                if (BuildConfig.DEBUG) {
                    OutlinedButton(onClick = { developerToolsOpen = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.developer_tools))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
    )
    if (developerToolsOpen) Dialog(onDismissRequest = { developerToolsOpen = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.developer_tools), style = MaterialTheme.typography.headlineSmall)
                    TextButton(onClick = { developerToolsOpen = false }) { Text(stringResource(R.string.close)) }
                }
                Text("Testdata og designværktøjer til udvikling. Funktionerne vises ikke i release-versionen.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(enabled = !creatingTestFamily, onClick = {
                    creatingTestFamily = true
                    onCreateDeveloperTestFamily { creatingTestFamily = false; testFamilyCreated = true }
                }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(if (creatingTestFamily) R.string.creating_test_family else R.string.create_test_family)) }
                Button(enabled = !creatingPaletteChildren, onClick = {
                    creatingPaletteChildren = true
                    onCreateDeveloperPaletteChildren { creatingPaletteChildren = false; paletteChildrenCreated = true }
                }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(if (creatingPaletteChildren) R.string.creating_palette_children else R.string.create_palette_children)) }
                OutlinedButton(onClick = { palettePreviewOpen = true }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.preview_color_profiles)) }
                OutlinedButton(onClick = { confirmClearData = true }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.clear_app_data), color = MaterialTheme.colorScheme.error) }
            }
        }
    }
    if (confirmClearData) {
        AlertDialog(
            onDismissRequest = { confirmClearData = false },
            title = { Text(stringResource(R.string.clear_app_data_title)) },
            text = { Text(stringResource(R.string.clear_app_data_message)) },
            confirmButton = {
                Button(onClick = {
                    context.getSystemService(ActivityManager::class.java).clearApplicationUserData()
                }) { Text(stringResource(R.string.clear_everything)) }
            },
            dismissButton = { TextButton(onClick = { confirmClearData = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
    if (testFamilyCreated) AlertDialog(
        onDismissRequest = { testFamilyCreated = false },
        title = { Text(stringResource(R.string.test_family_created_title)) },
        text = { Text(stringResource(R.string.test_family_created_message)) },
        confirmButton = { Button(onClick = { testFamilyCreated = false }) { Text(stringResource(R.string.ok)) } },
    )
    if (paletteChildrenCreated) AlertDialog(
        onDismissRequest = { paletteChildrenCreated = false },
        title = { Text(stringResource(R.string.palette_children_created_title)) },
        text = { Text(stringResource(R.string.palette_children_created_message)) },
        confirmButton = { Button(onClick = { paletteChildrenCreated = false }) { Text(stringResource(R.string.ok)) } },
    )
    if (palettePreviewOpen) ColorProfileManagerDialog(
        profiles = colorProfiles,
        usedProfileIds = usedColorProfileIds,
        onSave = onSaveColorProfile,
        onDelete = onDeleteColorProfile,
        onMove = onMoveColorProfile,
        exportJson = exportColorProfiles,
        onImport = importColorProfiles,
        onDismiss = { palettePreviewOpen = false },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsRegionDropdown(value: DanishRegion, onChange: (DanishRegion) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    fun label(region: DanishRegion) = when (region) {
        DanishRegion.Hovedstaden -> R.string.region_hovedstaden
        DanishRegion.Midtjylland -> R.string.region_midtjylland
        DanishRegion.Nordjylland -> R.string.region_nordjylland
        DanishRegion.Sjaelland -> R.string.region_sjaelland
        DanishRegion.Syddanmark -> R.string.region_syddanmark
    }
    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(
            value = stringResource(label(value)), onValueChange = {}, readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(expanded, { expanded = false }) {
            DanishRegion.entries.forEach { region -> DropdownMenuItem(
                text = { Text(stringResource(label(region))) }, onClick = { onChange(region); expanded = false },
            ) }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: ChildProfile,
    selected: Boolean,
    onView: () -> Unit,
    onEdit: () -> Unit,
    photoFile: File?,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val bitmap = remember(photoFile?.path, photoFile?.lastModified()) {
                photoFile?.let { BitmapFactory.decodeFile(it.path)?.asImageBitmap() }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = stringResource(R.string.profile_photo_description),
                    modifier = Modifier.size(52.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(profile.avatar.symbol, style = MaterialTheme.typography.headlineMedium)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(profile.name, style = MaterialTheme.typography.titleMedium)
                if (selected) Text(stringResource(R.string.active_child), color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onView) { Icon(Icons.Outlined.Visibility, stringResource(R.string.view_child)) }
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, stringResource(R.string.edit_child)) }
        }
    }
}

@Composable
private fun ReorderProfilesDialog(
    title: String,
    items: List<Triple<String, String, String>>,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var ordered by remember(items) { mutableStateOf(items) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.drag_to_reorder))
                ordered.forEach { item ->
                    var dragDistance by remember(item.first) { mutableStateOf(0f) }
                    Card(
                        modifier = Modifier.fillMaxWidth().pointerInput(item.first, ordered) {
                            detectDragGestures(
                                onDragEnd = { dragDistance = 0f },
                                onDragCancel = { dragDistance = 0f },
                            ) { change, amount ->
                                change.consume()
                                dragDistance += amount.y
                                if (abs(dragDistance) > 48f) {
                                    val from = ordered.indexOfFirst { it.first == item.first }
                                    val to = (from + if (dragDistance > 0) 1 else -1).coerceIn(0, ordered.lastIndex)
                                    if (from != to) {
                                        ordered = ordered.toMutableList().apply { add(to, removeAt(from)) }
                                    }
                                    dragDistance = 0f
                                }
                            }
                        },
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Icons.Outlined.DragHandle, null)
                            Text(item.third, style = MaterialTheme.typography.titleLarge)
                            Text(item.second, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(ordered.map { it.first }) }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun RelationsDialog(
    children: List<ChildProfile>,
    members: List<ParentProfile>,
    existingLinks: List<ChildParentLink>,
    onSave: (Set<ChildParentLink>) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedLinks by remember(existingLinks) { mutableStateOf(existingLinks.toSet()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.review_relations)) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Text(stringResource(R.string.review_relations_description)) }
                children.forEach { child ->
                    item(key = "relation-child-${child.id}") {
                        Text(child.name, style = MaterialTheme.typography.titleMedium)
                    }
                    items(members, key = { member -> "${child.id}-${member.id}" }) { member ->
                        val link = ChildParentLink(child.id, member.id)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Checkbox(
                                checked = link in selectedLinks,
                                onCheckedChange = { checked ->
                                    selectedLinks = if (checked) selectedLinks + link else selectedLinks - link
                                },
                            )
                            Column {
                                Text(member.name)
                                Text(roleLabel(member.role), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(selectedLinks) }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun ProfileViewDialog(
    profile: ChildProfile,
    units: MeasurementUnits,
    photoFile: File?,
    onEdit: () -> Unit,
    onDismiss: () -> Unit,
    parents: List<ParentProfile>,
    siblings: List<ChildProfile>,
    familyPhotoFile: (String?) -> File?,
    providers: List<CareProvider>,
) {
    val draft = profile.toDraft(units)
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val accent = MaterialTheme.colorScheme.primaryContainer
        Surface(modifier = Modifier.fillMaxSize(), contentColor = MaterialTheme.colorScheme.onSurface, color = MaterialTheme.colorScheme.surface) {
          CompositionLocalProvider(LocalChildAccent provides accent) {
            Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(profile.name, style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, stringResource(R.string.edit_child)) }
                }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        val bitmap = remember(photoFile?.path, photoFile?.lastModified()) { photoFile?.let { BitmapFactory.decodeFile(it.path)?.asImageBitmap() } }
                        if (bitmap != null) Image(bitmap, stringResource(R.string.profile_photo_description), Modifier.size(96.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        else Text(profile.avatar.symbol, style = MaterialTheme.typography.displayLarge)
                    }
                    item { ViewSection(R.string.basic_information) }
                    if (profile.nickname.isNotBlank()) item { ViewValue(R.string.nickname, profile.nickname) }
                    item { ViewValue(R.string.birth_status, stringResource(if (profile.birthStatus == dk.babyapp.data.profile.BirthStatus.Born) R.string.already_born else R.string.not_born_yet)) }
                    profile.birthDate?.let { item { ViewValue(R.string.birth_date, it.toString()) } }
                    profile.birthTime?.let { item { ViewValue(R.string.birth_time, it.toString()) } }
                    if (profile.birthStatus == dk.babyapp.data.profile.BirthStatus.Expected) profile.dueDate?.let { item { ViewValue(R.string.due_date, it.toString()) } }
                    item { ViewValue(R.string.biological_sex, stringResource(when (profile.sex) {
                        dk.babyapp.data.profile.BiologicalSex.Unselected -> R.string.select_sex
                        dk.babyapp.data.profile.BiologicalSex.PreferNotToSay -> R.string.sex_not_specified
                        dk.babyapp.data.profile.BiologicalSex.Female -> R.string.sex_female
                        dk.babyapp.data.profile.BiologicalSex.Male -> R.string.sex_male
                        dk.babyapp.data.profile.BiologicalSex.Other -> R.string.sex_other
                    })) }
                    item { ViewSection(R.string.birth_details) }
                    if (profile.birthStatus == dk.babyapp.data.profile.BirthStatus.Born) profile.dueDate?.let { item { ViewValue(R.string.due_date, it.toString()) } }
                    if (profile.hospital.isNotBlank()) item { ViewValue(R.string.birth_place, profile.hospital) }
                    if (draft.birthWeightGrams.isNotBlank()) item { ViewValue(if (units == MeasurementUnits.Metric) R.string.birth_weight else R.string.birth_weight_imperial, draft.birthWeightGrams) }
                    if (draft.birthLengthCm.isNotBlank()) item { ViewValue(if (units == MeasurementUnits.Metric) R.string.birth_length else R.string.birth_length_imperial, draft.birthLengthCm) }
                    if (draft.birthHeadCircumferenceCm.isNotBlank()) item { ViewValue(if (units == MeasurementUnits.Metric) R.string.head_circumference else R.string.head_circumference_imperial, draft.birthHeadCircumferenceCm) }
                    item { ViewSection(R.string.care_details) }
                    items(providers, key = { it.id }) { provider ->
                        ViewValue(provider.type.labelRes(), listOf(provider.customTitle, provider.name, provider.phone, provider.email, provider.address, provider.notes).filter(String::isNotBlank).joinToString("\n"))
                    }
                    item { ViewSection(R.string.health_information) }
                    if (profile.allergies.isNotBlank()) item { ViewValue(R.string.allergies, profile.allergies) }
                    if (profile.medicalNotes.isNotBlank()) item { ViewValue(R.string.medical_notes, profile.medicalNotes) }
                    item { ViewSection(R.string.registry_information) }
                    if (profile.fullName.isNotBlank()) item { ViewValue(R.string.full_name, profile.fullName) }
                    if (profile.cprNumber.isNotBlank()) item { ViewValue(R.string.cpr_number, profile.cprNumber) }
                    if (profile.registeredAddress.isNotBlank()) item { ViewValue(R.string.registered_address, profile.registeredAddress) }
                    if (profile.nationality.isNotBlank()) item { ViewValue(R.string.nationality, profile.nationality) }
                    val linkedParents = parents.filter { it.role.isParent }
                    val otherMembers = parents.filterNot { it.role.isParent }
                    item { ViewSection(R.string.parents) }
                    if (linkedParents.isEmpty()) item { Text(stringResource(R.string.no_parents_linked)) }
                    items(linkedParents, key = { it.id }) { member -> FamilyMemberView(member, familyPhotoFile(member.photoFileName)) }
                    if (siblings.isNotEmpty()) {
                        item { ViewSection(R.string.siblings) }
                        items(siblings, key = { it.id }) { sibling -> Text("${sibling.avatar.symbol} ${sibling.name}") }
                    }
                    item { ViewSection(R.string.other_family_members) }
                    if (otherMembers.isEmpty()) item { Text(stringResource(R.string.no_other_family_linked)) }
                    items(otherMembers, key = { it.id }) { member -> FamilyMemberView(member, familyPhotoFile(member.photoFileName)) }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text(stringResource(R.string.close)) }
            }
          }
        }
    }
}

private val LocalChildAccent = staticCompositionLocalOf { Color(0xFFF5CAD2) }
@Composable private fun ViewSection(label: Int) {
    Surface(color = LocalChildAccent.current, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(label), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp))
    }
}
@Composable private fun ViewValue(label: Int, value: String) { Column { Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value) } }

@Composable private fun FamilyMemberView(member: ParentProfile, photoFile: File?) {
    Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            val bitmap = remember(photoFile?.path, photoFile?.lastModified()) { photoFile?.let { BitmapFactory.decodeFile(it.path)?.asImageBitmap() } }
            if (bitmap != null) Image(bitmap, null, Modifier.size(48.dp).clip(CircleShape), contentScale = ContentScale.Crop) else Text(member.avatar.symbol, style = MaterialTheme.typography.headlineMedium)
            Column { Text(roleLabel(member.role), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary); Text(member.name, style = MaterialTheme.typography.titleMedium); if (member.phone.isNotBlank()) Text(member.phone); if (member.email.isNotBlank()) Text(member.email) }
        }
    }
}

@Composable private fun roleLabel(role: FamilyMemberRole): String = stringResource(when (role) {
    FamilyMemberRole.Mother -> R.string.role_mother
    FamilyMemberRole.Father -> R.string.role_father
    FamilyMemberRole.CoMother -> R.string.role_co_mother
    FamilyMemberRole.CoFather -> R.string.role_co_father
    FamilyMemberRole.Parent -> R.string.role_parent
    FamilyMemberRole.ParentNotSpecified -> R.string.role_parent_unspecified
    FamilyMemberRole.Grandmother -> R.string.role_grandmother
    FamilyMemberRole.Grandfather -> R.string.role_grandfather
    FamilyMemberRole.Grandparent -> R.string.role_grandparent
    FamilyMemberRole.BonusMother -> R.string.role_bonus_mother
    FamilyMemberRole.BonusFather -> R.string.role_bonus_father
    FamilyMemberRole.BonusParent -> R.string.role_bonus_parent
    FamilyMemberRole.Sister -> R.string.role_sister
    FamilyMemberRole.Brother -> R.string.role_brother
    FamilyMemberRole.Sibling -> R.string.role_sibling
    FamilyMemberRole.BonusSibling -> R.string.role_bonus_sibling
    FamilyMemberRole.Aunt -> R.string.role_aunt
    FamilyMemberRole.Uncle -> R.string.role_uncle
    FamilyMemberRole.MaternalAunt -> R.string.role_maternal_aunt
    FamilyMemberRole.PaternalAunt -> R.string.role_paternal_aunt
    FamilyMemberRole.MaternalUncle -> R.string.role_maternal_uncle
    FamilyMemberRole.PaternalUncle -> R.string.role_paternal_uncle
    FamilyMemberRole.Cousin -> R.string.role_cousin
    FamilyMemberRole.OtherNotSpecified -> R.string.role_not_specified
})

@Composable
private fun ProfileEditorDialog(
    existing: ChildProfile?,
    onSaveProfile: (ProfileDraft, (ProfileValidationError?) -> Unit) -> Unit,
    photoFile: (String?) -> File?,
    onPhotoSelected: suspend (Uri) -> String,
    onDismiss: () -> Unit,
    units: MeasurementUnits,
    onDelete: (() -> Unit)?,
    parents: List<ParentProfile>,
    linkedParentIds: Set<String>,
    onSaveParent: (ParentProfile, Set<String>) -> Unit,
    onDeleteParent: (ParentProfile) -> Unit,
    children: List<ChildProfile>,
    familyLinks: List<ChildParentLink>,
    careProviders: List<CareProvider>,
    colorProfiles: List<ColorProfile>,
) {
    var draft by remember(existing?.id, units) { mutableStateOf(existing?.toDraft(units, linkedParentIds, careProviders) ?: ProfileDraft()) }
    var error by remember { mutableStateOf<ProfileValidationError?>(null) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(if (existing == null) R.string.add_child else R.string.edit_child),
                    style = MaterialTheme.typography.headlineSmall,
                )
                ProfileForm(
                    draft = draft,
                    onDraftChange = { draft = it; error = null },
                    photoFile = photoFile(draft.photoFileName),
                    onPhotoSelected = onPhotoSelected,
                    units = units,
                    colorProfiles = colorProfiles,
                    modifier = Modifier.weight(1f),
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (onDelete != null) TextButton(onClick = onDelete) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    Button(onClick = {
                        onSaveProfile(draft) { result -> error = result; if (result == null) onDismiss() }
                    }) { Text(stringResource(R.string.save)) }
                }
            }
        }
    }
    error?.let { validationError ->
        AlertDialog(
            onDismissRequest = { error = null },
            title = { Text(stringResource(R.string.check_information)) },
            text = { Text(stringResource(validationError.messageRes())) },
            confirmButton = { TextButton(onClick = { error = null }) { Text(stringResource(R.string.ok)) } },
        )
    }
}

@Composable
private fun ParentsEditor(
    parents: List<ParentProfile>, selected: Set<String>, onSelectedChange: (Set<String>) -> Unit,
    children: List<ChildProfile>, currentChildId: String?, familyLinks: List<ChildParentLink>, photoFile: (String?) -> File?, onPhotoSelected: suspend (Uri) -> String, onSave: (ParentProfile, Set<String>) -> Unit, onDelete: (ParentProfile) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ParentProfile?>(null) }
    var creating by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Button(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.family_title) + if (expanded) " ▲" else " ▼") }
        if (expanded) {
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                parents.forEach { parent ->
                    FilterChip(
                        selected = parent.id in selected,
                        onClick = { onSelectedChange(if (parent.id in selected) selected - parent.id else selected + parent.id) },
                        label = { Text("${parent.avatar.symbol} ${parent.name}") },
                    )
                    IconButton(onClick = { editing = parent }) { Icon(Icons.Outlined.Edit, stringResource(R.string.edit_parent)) }
                }
            }
            TextButton(onClick = { creating = true }) { Text(stringResource(R.string.add_parent)) }
        }
    }
    if (creating || editing != null) ParentEditorDialog(
        existing = editing, initialRole = FamilyMemberRole.ParentNotSpecified, children = children,
        selectedChildIds = editing?.let { member -> familyLinks.filter { it.parentId == member.id }.map { it.childId }.toSet() }
            ?: currentChildId?.let(::setOf).orEmpty(), onSave = onSave,
        onDelete = { parent -> onDelete(parent); editing = null }, photoFile = photoFile, onPhotoSelected = onPhotoSelected, onDismiss = { creating = false; editing = null },
    )
}

@Composable
private fun ParentEditorDialog(
    existing: ParentProfile?, initialRole: FamilyMemberRole, children: List<ChildProfile>, selectedChildIds: Set<String>,
    onSave: (ParentProfile, Set<String>) -> Unit, onDelete: (ParentProfile) -> Unit, photoFile: (String?) -> File?, onPhotoSelected: suspend (Uri) -> String, onDismiss: () -> Unit,
) {
    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var phone by remember(existing?.id) { mutableStateOf(existing?.phone.orEmpty()) }
    var email by remember(existing?.id) { mutableStateOf(existing?.email.orEmpty()) }
    var cpr by remember(existing?.id) { mutableStateOf(existing?.cprNumber.orEmpty()) }
    var avatar by remember(existing?.id) { mutableStateOf(existing?.avatar ?: dk.babyapp.data.profile.ProfileAvatar.Bear) }
    var role by remember(existing?.id) { mutableStateOf(existing?.role ?: initialRole) }
    var notes by remember(existing?.id) { mutableStateOf(existing?.notes.orEmpty()) }
    var childIds by remember(existing?.id) { mutableStateOf(selectedChildIds) }
    var confirmDelete by remember { mutableStateOf(false) }
    var missingChildError by remember { mutableStateOf(false) }
    var photoName by remember(existing?.id) { mutableStateOf(existing?.photoFileName) }
    val photoScope = rememberCoroutineScope()
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) photoScope.launch { photoName = onPhotoSelected(uri) } }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(if (role.isParent) (if (existing == null) R.string.add_parent else R.string.edit_parent) else (if (existing == null) R.string.add_family_profile else R.string.edit_family_profile)), style = MaterialTheme.typography.headlineSmall)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.choose_avatar), style = MaterialTheme.typography.labelLarge)
            photoFile(photoName)?.let { file -> BitmapFactory.decodeFile(file.path)?.asImageBitmap()?.let { bitmap -> Image(bitmap, stringResource(R.string.profile_photo_description), Modifier.size(72.dp).clip(CircleShape), contentScale = ContentScale.Crop) } }
            TextButton(onClick = { photoLauncher.launch("image/*") }) { Text(stringResource(R.string.choose_photo)) }
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { dk.babyapp.data.profile.ProfileAvatar.entries.forEach { option -> FilterChip(avatar == option, { avatar = option }, label = { Text(option.symbol) }) } }
            androidx.compose.material3.OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.parent_name)) })
            androidx.compose.material3.OutlinedTextField(phone, { phone = it }, label = { Text(stringResource(R.string.phone_number)) })
            androidx.compose.material3.OutlinedTextField(email, { email = it }, label = { Text(stringResource(R.string.email)) })
            androidx.compose.material3.OutlinedTextField(cpr, { cpr = it }, label = { Text(stringResource(R.string.cpr_number)) })
            FamilyRoleDropdown(role, role.isParent) { role = it }
            androidx.compose.material3.OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(R.string.notes)) })
            Text(stringResource(R.string.link_to_children))
                androidx.compose.foundation.layout.FlowRow { children.forEach { child -> FilterChip(child.id in childIds, { childIds = if (child.id in childIds) childIds - child.id else childIds + child.id }, label = { Text(child.name) }) } }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (existing != null) TextButton(onClick = { confirmDelete = true }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                Button(enabled = name.isNotBlank(), onClick = {
                    if (childIds.isEmpty()) missingChildError = true else {
                        onSave(ParentProfile(existing?.id ?: java.util.UUID.randomUUID().toString(), name.trim(), phone.trim(), email.trim(), cpr.trim(), avatar, role, notes.trim(), photoName), childIds); onDismiss()
                    }
                }) { Text(stringResource(R.string.save)) }
            }
            }
        }
    }
    if (confirmDelete && existing != null) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text(stringResource(R.string.delete_parent_title, existing.name)) },
        text = { Text(stringResource(R.string.delete_parent_message)) },
        confirmButton = { Button(onClick = { onDelete(existing); onDismiss() }) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } },
    )
    if (missingChildError) AlertDialog(
        onDismissRequest = { missingChildError = false },
        title = { Text(stringResource(R.string.child_link_required_title)) },
        text = { Text(stringResource(R.string.child_link_required_message)) },
        confirmButton = { TextButton(onClick = { missingChildError = false }) { Text(stringResource(R.string.ok)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FamilyRoleDropdown(value: FamilyMemberRole, parentProfile: Boolean, onChange: (FamilyMemberRole) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(value = roleLabel(value), onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable), label = { Text(stringResource(if (parentProfile) R.string.parent_role else R.string.family_role)) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
        val options = FamilyMemberRole.entries.filter { it.isParent == parentProfile }
        ExposedDropdownMenu(expanded, { expanded = false }) { options.forEach { role -> DropdownMenuItem(text = { Text(roleLabel(role)) }, onClick = { onChange(role); expanded = false }) } }
    }
}
