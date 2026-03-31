package com.habitslab.domain.repository

import com.habitslab.domain.model.Habit
import com.habitslab.domain.model.HabitRecord
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    suspend fun addHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    fun getRecordsForHabit(habitId: Long): Flow<List<HabitRecord>>
    fun getRecordsForToday(): Flow<List<HabitRecord>>
    suspend fun toggleHabitCompletion(habitId: Long, date: java.time.LocalDate)
}
