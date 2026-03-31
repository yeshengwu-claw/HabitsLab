package com.habitslab.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Habit(
    val id: Long = 0,
    val name: String,
    val icon: String = "✓",
    val color: Long = 0xFF4CAF50,
    val reminderTime: LocalTime? = null,
    val createdAt: LocalDate = LocalDate.now()
)

data class HabitRecord(
    val id: Long = 0,
    val habitId: Long,
    val date: LocalDate,
    val completed: Boolean = true
)
