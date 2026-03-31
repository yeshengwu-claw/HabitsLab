package com.habitslab.di

import android.content.Context
import androidx.room.Room
import com.habitslab.data.local.HabitsDatabase
import com.habitslab.data.local.dao.HabitDao
import com.habitslab.data.repository.HabitRepositoryImpl
import com.habitslab.domain.repository.HabitRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabitsDatabase {
        return Room.databaseBuilder(
            context,
            HabitsDatabase::class.java,
            "habits_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideHabitDao(database: HabitsDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    @Singleton
    fun provideHabitRepository(dao: HabitDao): HabitRepository {
        return HabitRepositoryImpl(dao)
    }
}
