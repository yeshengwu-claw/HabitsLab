package com.habitslab.data.local

import com.habitslab.data.local.entity.HabitEntity
import com.habitslab.data.local.entity.HabitRecordEntity
import com.habitslab.domain.model.Habit
import com.habitslab.domain.model.HabitRecord
import java.time.LocalDate
import java.time.LocalTime

fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    name = name,
    icon = icon,
    color = color,
    reminderTime = reminderTime?.let { LocalTime.parse(it) },
    createdAt = LocalDate.parse(createdAt)
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
    reminderTime = reminderTime?.toString(),
    createdAt = createdAt.toString()
)

fun HabitRecordEntity.toDomain(): HabitRecord = HabitRecord(
    id = id,
    habitId = habitId,
    date = LocalDate.parse(date),
    completed = completed
)

fun HabitRecord.toEntity(): HabitRecordEntity = HabitRecordEntity(
    id = id,
    habitId = habitId,
    date = date.toString(),
    completed = completed
)
