package com.habitslab.data.local.dao

import androidx.room.*
import com.habitslab.data.local.entity.HabitEntity
import com.habitslab.data.local.entity.HabitRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId ORDER BY date DESC")
    fun getRecordsForHabit(habitId: Long): Flow<List<HabitRecordEntity>>

    @Query("SELECT * FROM habit_records WHERE date = :date")
    fun getRecordsForDate(date: String): Flow<List<HabitRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HabitRecordEntity)

    @Query("DELETE FROM habit_records WHERE habitId = :habitId AND date = :date")
    suspend fun deleteRecord(habitId: Long, date: String)
}
