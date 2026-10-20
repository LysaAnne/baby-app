package dk.babyapp.ui.theme

import androidx.compose.ui.unit.dp

/** The few distances layouts use, so cards, lists and dialogs line up the same everywhere. */
object BabySpacing {
    /** Between an icon and its label, or tightly related lines of text. */
    val xs = 4.dp
    /** Between buttons in a row and items inside a card. */
    val s = 8.dp
    /** Between cards in a list. */
    val m = 12.dp
    /** Screen edges and the inside of cards. */
    val l = 16.dp
    /** Between sections of a screen. */
    val xl = 24.dp
}

/** Minimum sizes for things people tap, also with one hand and tired eyes. */
object BabyTouch {
    /** Every tappable element (Material and WCAG minimum). */
    val minimum = 48.dp
    /** The main actions people use many times a day, like quick registration. */
    val primary = 56.dp
}
