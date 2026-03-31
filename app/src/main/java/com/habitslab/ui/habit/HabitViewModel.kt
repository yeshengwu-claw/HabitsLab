package com.habitslab.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitslab.domain.model.Habit
import com.habitslab.domain.model.HabitRecord
import com.habitslab.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HabitWithTodayRecord(
    val habit: Habit,
    val completedToday: Boolean,
    val streak: Int
)

data class HabitUiState(
    val habits: List<HabitWithTodayRecord> = emptyList(),
    val showAddDialog: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            combine(
                repository.getAllHabits(),
                repository.getRecordsForToday()
            ) { habits, todayRecords ->
                habits.map { habit ->
                    HabitWithTodayRecord(
                        habit = habit,
                        completedToday = todayRecords.any { it.habitId == habit.id },
                        streak = calculateStreak(habit.id)
                    )
                }
            }.collect { habitsWithRecords ->
                _uiState.update {
                    it.copy(habits = habitsWithRecords, isLoading = false)
                }
            }
        }
    }

    private suspend fun calculateStreak(habitId: Long): Int {
        var streak = 0
        var date = LocalDate.now()
        repository.getRecordsForHabit(habitId).first().forEach { record ->
            if (record.date == date && record.completed) {
                streak++
                date = date.minusDays(1)
            }
        }
        return streak
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun addHabit(name: String, icon: String, color: Long) {
        viewModelScope.launch {
            repository.addHabit(Habit(name = name, icon = icon, color = color))
            hideAddDialog()
        }
    }

    fun toggleHabitCompletion(habitId: Long) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, LocalDate.now())
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }
}
