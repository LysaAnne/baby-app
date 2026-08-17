package dk.babyapp.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ViewTimeline
import androidx.compose.ui.graphics.vector.ImageVector
import dk.babyapp.R
import kotlinx.serialization.Serializable

sealed interface AppDestination {
    @get:StringRes
    val labelRes: Int
    val icon: ImageVector

    @Serializable
    data object Today : AppDestination {
        override val labelRes = R.string.nav_today
        override val icon = Icons.Outlined.Home
    }

    @Serializable
    data object Timeline : AppDestination {
        override val labelRes = R.string.nav_timeline
        override val icon = Icons.Outlined.ViewTimeline
    }

    @Serializable
    data object Insights : AppDestination {
        override val labelRes = R.string.nav_insights
        override val icon = Icons.Outlined.AutoGraph
    }

    @Serializable
    data object Guide : AppDestination {
        override val labelRes = R.string.nav_guide
        override val icon = Icons.AutoMirrored.Outlined.MenuBook
    }

    @Serializable
    data object Family : AppDestination {
        override val labelRes = R.string.nav_family
        override val icon = Icons.Outlined.FamilyRestroom
    }

    companion object {
        val topLevel = listOf(Today, Timeline, Insights, Guide, Family)
    }
}
