package dk.babyapp.ui.profile

import androidx.annotation.StringRes
import dk.babyapp.R
import dk.babyapp.data.profile.CareProviderType

@StringRes
fun CareProviderType.labelRes() = when (this) {
    CareProviderType.Hospital -> R.string.birth_hospital
    CareProviderType.Gp -> R.string.gp
    CareProviderType.HealthVisitor -> R.string.health_visitor
    CareProviderType.Midwife -> R.string.midwife
    CareProviderType.Specialist -> R.string.specialist
    CareProviderType.Other -> R.string.other_health_professional
}
