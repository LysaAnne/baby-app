package dk.babyapp.ui.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.babyapp.data.book.BabyBookPage
import dk.babyapp.data.book.BabyBookRepository
import dk.babyapp.data.preferences.AppPreferencesRepository
import dk.babyapp.data.preferences.BookReminderFrequency
import javax.inject.Inject
import kotlinx.coroutines.launch

/** Barnets bog: saving pages and the book reminder preferences. */
@HiltViewModel
class BookViewModel @Inject constructor(
    private val preferencesRepository: AppPreferencesRepository,
    private val bookRepository: BabyBookRepository? = null,
) : ViewModel() {
    suspend fun saveBookPage(page: BabyBookPage) { requireNotNull(bookRepository).save(page) }
    fun updateBookReminders(enabled: Boolean) = viewModelScope.launch { preferencesRepository.updateBookReminders(enabled) }
    fun updateBookReminderFrequency(frequency: BookReminderFrequency) = viewModelScope.launch { preferencesRepository.updateBookReminderFrequency(frequency) }
}
