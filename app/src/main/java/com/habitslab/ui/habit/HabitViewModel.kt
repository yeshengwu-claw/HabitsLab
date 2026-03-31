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
import java.time.YearMonth
import javax.inject.Inject

data class HabitWithTodayRecord(
    val habit: Habit,
    val completedToday: Boolean,
    val streak: Int,
    val totalCompleted: Int,
    val weeklyRate: Float,
    val records: List<HabitRecord> = emptyList()
)

data class HabitUiState(
    val habits: List<HabitWithTodayRecord> = emptyList(),
    val showAddDialog: Boolean = false,
    val selectedHabit: HabitWithTodayRecord? = null,
    val showDetailSheet: Boolean = false,
    val selectedMonth: YearMonth = YearMonth.now(),
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
                    val records = repository.getRecordsForHabit(habit.id).first()
                    val completedToday = todayRecords.any { it.habitId == habit.id }
                    val streak = calculateStreak(habit.id, records)
                    val weeklyRate = calculateWeeklyRate(habit.id, records)
                    HabitWithTodayRecord(
                        habit = habit,
                        completedToday = completedToday,
                        streak = streak,
                        totalCompleted = records.count { it.completed },
                        weeklyRate = weeklyRate,
                        records = records
                    )
                }
            }.collect { habitsWithRecords ->
                _uiState.update {
                    it.copy(habits = habitsWithRecords, isLoading = false)
                }
            }
        }
    }

    private suspend fun calculateStreak(habitId: Long, allRecords: List<HabitRecord>): Int {
        val records = allRecords.sortedByDescending { it.date }
        var streak = 0
        var date = LocalDate.now()

        // 如果今天没打卡但昨天打了，也算连续
        val todayRecord = records.find { it.date == date }
        if (todayRecord == null) {
            date = date.minusDays(1)
        }

        for (record in records) {
            if (record.date == date && record.completed) {
                streak++
                date = date.minusDays(1)
            } else if (record.date.isBefore(date)) {
                break
            }
        }
        return streak
    }

    private fun calculateWeeklyRate(habitId: Long, allRecords: List<HabitRecord>): Float {
        val sevenDaysAgo = LocalDate.now().minusDays(7)
        val recentRecords = allRecords.filter { it.date.isAfter(sevenDaysAgo) && it.completed }
        return recentRecords.size / 7f
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

    fun selectHabit(habitWithRecord: HabitWithTodayRecord) {
        _uiState.update {
            it.copy(selectedHabit = habitWithRecord, showDetailSheet = true)
        }
    }

    fun hideDetailSheet() {
        _uiState.update { it.copy(showDetailSheet = false, selectedHabit = null) }
    }

    fun previousMonth() {
        _uiState.update { it.copy(selectedMonth = it.selectedMonth.minusMonths(1)) }
    }

    fun nextMonth() {
        _uiState.update { it.copy(selectedMonth = it.selectedMonth.plusMonths(1)) }
    }
}
