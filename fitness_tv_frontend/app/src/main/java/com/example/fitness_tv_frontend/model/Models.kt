package com.example.fitness_tv_frontend.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * Data models for the Fitness TV app using mock/local data
 */

// PUBLIC_INTERFACE
@Parcelize
data class Workout(
    val id: String,
    val title: String,
    val category: String,
    val durationMin: Int,
    val intensity: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val caloriesEstimate: Int
) : Parcelable

// PUBLIC_INTERFACE
@Parcelize
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String
) : Parcelable

// PUBLIC_INTERFACE
@Parcelize
data class Profile(
    val name: String,
    val age: Int,
    val weightKg: Int,
    val heightCm: Int,
    val goal: Goal
) : Parcelable

// PUBLIC_INTERFACE
@Parcelize
data class Goal(
    val weeklyWorkouts: Int,
    val targetCaloriesPerWeek: Int
) : Parcelable

// PUBLIC_INTERFACE
data class ProgressEntry(
    val date: LocalDate,
    val calories: Int,
    val minutes: Int
)
