package com.habitslab.data.repository

import com.habitslab.data.local.dao.HabitDao
import com.habitslab.data.local.entity.HabitRecordEntity
import com.habitslab.data.local.toDomain
import com.habitslab.data.local.toEntity
import com.habitslab.domain.model.Habit
import com.habitslab.domain.model.HabitRecord
import com.habitslab.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val dao: HabitDao
) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> =
        dao.getAllHabits().map { list -> list.map { it.toDomain() } }

    override suspend fun addHabit(habit: Habit): Long =
        dao.insertHabit(habit.toEntity())

    override suspend fun updateHabit(habit: Habit) =
        dao.updateHabit(habit.toEntity())

    override suspend fun deleteHabit(habit: Habit) =
        dao.deleteHabit(habit.toEntity())

    override fun getRecordsForHabit(habitId: Long): Flow<List<HabitRecord>> =
        dao.getRecordsForHabit(habitId).map { list -> list.map { it.toDomain() } }

    override fun getRecordsForToday(): Flow<List<HabitRecord>> =
        dao.getRecordsForDate(LocalDate.now().toString()).map { list -> list.map { it.toDomain() } }

    override suspend fun toggleHabitCompletion(habitId: Long, date: LocalDate) {
        val dateStr = date.toString()
        val records = dao.getRecordsForDate(dateStr).first()
        val existing = records.find { it.habitId == habitId }
        if (existing != null) {
            dao.deleteRecord(habitId, dateStr)
        } else {
            dao.insertRecord(
                HabitRecordEntity(
                    habitId = habitId,
                    date = dateStr,
                    completed = true
                )
            )
        }
    }
}
