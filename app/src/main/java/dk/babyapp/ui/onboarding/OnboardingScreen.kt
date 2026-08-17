package dk.babyapp.ui.onboarding

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import dk.babyapp.R
import dk.babyapp.data.preferences.DanishRegion
import dk.babyapp.data.preferences.AppPreferences
import dk.babyapp.data.preferences.MeasurementUnits
import dk.babyapp.data.preferences.ThemePreference
import dk.babyapp.ui.AppViewModel
import dk.babyapp.ui.OnboardingSettings

@Composable
fun OnboardingScreen(viewModel: AppViewModel, preferences: AppPreferences) {
    val settings = OnboardingSettings(languageTag = "da", region = preferences.region, units = preferences.units, theme = preferences.theme)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WelcomeStep()
            Button(onClick = { viewModel.completeOnboarding(settings) {} }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.continue_label)) }
        }
    }
}

@Composable
private fun LanguageStep(languageTag: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.choose_language), style = MaterialTheme.typography.headlineSmall)
        listOf("da" to R.string.danish, "en" to R.string.english).forEach { (tag, label) ->
            FilterChip(selected = languageTag == tag, onClick = { onChange(tag) }, label = { Text(stringResource(label)) })
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.welcome_title), style = MaterialTheme.typography.displaySmall)
        Text(stringResource(R.string.welcome_body), style = MaterialTheme.typography.bodyLarge)
        Text(stringResource(R.string.local_privacy_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.local_privacy_body), style = MaterialTheme.typography.bodyMedium)
        Text(
            stringResource(R.string.medical_boundary),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun PreferencesStep(settings: OnboardingSettings, onChange: (OnboardingSettings) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.preferences_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.region), style = MaterialTheme.typography.titleSmall)
        RegionDropdown(settings.region) { onChange(settings.copy(region = it)) }
        Text(stringResource(R.string.units), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MeasurementUnits.entries.forEach { units ->
                FilterChip(selected = settings.units == units, onClick = { onChange(settings.copy(units = units)) }, label = {
                    Text(stringResource(if (units == MeasurementUnits.Metric) R.string.units_metric else R.string.units_imperial))
                })
            }
        }
        Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ThemePreference.entries.forEach { theme ->
                FilterChip(
                    selected = settings.theme == theme,
                    onClick = { onChange(settings.copy(theme = theme)) },
                    label = { Text(stringResource(theme.labelRes())) },
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun RegionDropdown(value: DanishRegion, onChange: (DanishRegion) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(
            value = value.displayName(), onValueChange = {}, readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            label = { Text(stringResource(R.string.region)) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(expanded, { expanded = false }) {
            DanishRegion.entries.forEach { region -> DropdownMenuItem(text = { Text(region.displayName()) }, onClick = { onChange(region); expanded = false }) }
        }
    }
}

@Composable
private fun DanishRegion.displayName(): String = when (this) {
    DanishRegion.Hovedstaden -> stringResource(R.string.region_hovedstaden)
    DanishRegion.Midtjylland -> stringResource(R.string.region_midtjylland)
    DanishRegion.Nordjylland -> stringResource(R.string.region_nordjylland)
    DanishRegion.Sjaelland -> stringResource(R.string.region_sjaelland)
    DanishRegion.Syddanmark -> stringResource(R.string.region_syddanmark)
}

private fun ThemePreference.labelRes() = when (this) {
    ThemePreference.System -> R.string.theme_system
    ThemePreference.Light -> R.string.theme_light
    ThemePreference.Dark -> R.string.theme_dark
}
