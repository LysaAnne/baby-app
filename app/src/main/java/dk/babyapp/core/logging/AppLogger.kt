package dk.babyapp.core.logging

/**
 * Logging boundary for the application.
 *
 * Logs must never contain names, dates of birth, health records, free-text notes,
 * attachment paths, identifiers, or other personal data.
 */
interface AppLogger {
    fun debug(event: String)
}

