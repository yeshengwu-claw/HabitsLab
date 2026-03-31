package com.habitslab.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.habitslab.data.local.dao.HabitDao
import com.habitslab.data.local.entity.HabitEntity
import com.habitslab.data.local.entity.HabitRecordEntity

@Database(
    entities = [HabitEntity::class, HabitRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HabitsDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}
