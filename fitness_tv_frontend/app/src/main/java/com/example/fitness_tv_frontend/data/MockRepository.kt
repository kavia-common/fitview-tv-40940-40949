package com.example.fitness_tv_frontend.data

import android.content.Context
import android.content.SharedPreferences
import com.example.fitness_tv_frontend.model.Badge
import com.example.fitness_tv_frontend.model.Goal
import com.example.fitness_tv_frontend.model.Profile
import com.example.fitness_tv_frontend.model.ProgressEntry
import com.example.fitness_tv_frontend.model.Workout
import java.time.LocalDate
import kotlin.math.max

/**
 * Mock repository providing local data and simple persistence for profile/goals.
 */
class MockRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("fitness_tv_prefs", Context.MODE_PRIVATE)

    private val sampleVideo = "https://storage.googleapis.com/exoplayer-test-media-1/multi-bitrate/hls/manifest.m3u8"

    private val workouts = listOf(
        Workout("w1", "Morning HIIT Blast", "Daily", 15, "High", sampleVideo, "", 120),
        Workout("w2", "Core Strength Flow", "Recommended", 20, "Medium", sampleVideo, "", 150),
        Workout("w3", "Full Body Stretch", "Recommended", 10, "Low", sampleVideo, "", 60),
        Workout("w4", "Cardio Burn", "Continue", 25, "High", sampleVideo, "", 220),
        Workout("w5", "Yoga Balance", "Recommended", 18, "Low", sampleVideo, "", 90),
        Workout("w6", "Upper Body Power", "Recommended", 22, "Medium", sampleVideo, "", 170),
    )

    private val badges = listOf(
        Badge("b1", "Starter", "Completed your first workout", "🏁"),
        Badge("b2", "Consistency", "3 days in a row", "🔥"),
        Badge("b3", "Calorie Crusher", "500+ weekly calories", "💪"),
        Badge("b4", "Streak", "7-day streak", "✨")
    )

    private fun defaultProfile(): Profile {
        val goal = Goal(weeklyWorkouts = 4, targetCaloriesPerWeek = 1200)
        return Profile(
            name = prefs.getString("profile_name", "You") ?: "You",
            age = prefs.getInt("profile_age", 28),
            weightKg = prefs.getInt("profile_weight", 70),
            heightCm = prefs.getInt("profile_height", 170),
            goal = goalFromPrefs()
        )
    }

    private fun goalFromPrefs(): Goal {
        return Goal(
            weeklyWorkouts = prefs.getInt("goal_weekly_workouts", 4),
            targetCaloriesPerWeek = prefs.getInt("goal_target_cal", 1200)
        )
    }

    fun getProfile(): Profile = defaultProfile()

    fun updateProfile(profile: Profile) {
        prefs.edit()
            .putString("profile_name", profile.name)
            .putInt("profile_age", profile.age)
            .putInt("profile_weight", profile.weightKg)
            .putInt("profile_height", profile.heightCm)
            .apply()
        updateGoal(profile.goal)
    }

    fun updateGoal(goal: Goal) {
        prefs.edit()
            .putInt("goal_weekly_workouts", goal.weeklyWorkouts)
            .putInt("goal_target_cal", goal.targetCaloriesPerWeek)
            .apply()
    }

    fun getDailyOrContinue(): List<Workout> =
        workouts.filter { it.category == "Daily" || it.category == "Continue" }

    fun getRecommended(profile: Profile = getProfile()): List<Workout> {
        // Base intensity preference from profile goal
        val baseIntensity = when {
            profile.goal.weeklyWorkouts >= 5 -> "High"
            profile.goal.weeklyWorkouts >= 3 -> "Medium"
            else -> "Low"
        }

        // Read onboarding preferences (if any)
        val goals = prefs.getStringSet("pref_goals", emptySet()) ?: emptySet()
        val prefDuration = prefs.getInt("pref_duration", 20)

        // Map goals to an intensity bias
        val biasIntensity = when {
            goals.contains("weight_loss") || goals.contains("cardio") -> "High"
            goals.contains("strength") -> "Medium"
            goals.contains("flexibility") -> "Low"
            else -> baseIntensity
        }

        // Score and sort by relevance:
        // +2 match intensity bias, +1 if category is Recommended, +duration closeness (<=7 -> +2, <=14 -> +1)
        val durationWindow = 7
        val scored = workouts.map { w ->
            val intensityScore = if (w.intensity == biasIntensity) 2 else 0
            val categoryScore = if (w.category == "Recommended") 1 else 0
            val durationDelta = kotlin.math.abs(w.durationMin - prefDuration)
            val durationScore = if (durationDelta <= durationWindow) 2 else if (durationDelta <= durationWindow * 2) 1 else 0
            val total = intensityScore + categoryScore + durationScore
            w to total
        }

        var recs = scored
            .sortedWith(compareByDescending<Pair<Workout, Int>> { it.second }
                .thenBy { kotlin.math.abs(it.first.durationMin - prefDuration) })
            .map { it.first }
            .filter { it.category == "Recommended" || it.intensity == biasIntensity }

        // Fallback: ensure we never return empty
        if (recs.isEmpty()) {
            recs = workouts.filter { it.category == "Recommended" }
                .ifEmpty { workouts }
        }
        return recs
    }

    fun getBadges(): List<Badge> = badges

    fun getWorkouts(): List<Workout> = workouts

    fun searchWorkouts(query: String): List<Workout> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return workouts
        return workouts.filter {
            it.title.lowercase().contains(q) || it.category.lowercase().contains(q) || it.intensity.lowercase().contains(q)
        }
    }

    fun recentProgress(): List<ProgressEntry> {
        val today = LocalDate.now()
        // Mock last 7 days calories with a slight wave pattern
        return (0..6).map { i ->
            val day = today.minusDays((6 - i).toLong())
            val calories = 80 + (i * 35) % 220
            val minutes = max(8, calories / 10)
            ProgressEntry(day, calories, minutes)
        }
    }
}
