package com.example.fitness_tv_frontend.ui.onboarding

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper for persisting onboarding-related preferences.
 * Uses the same SharedPreferences file as the rest of the app to keep data centralized.
 */
class OnboardingPrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    fun isCompleted(): Boolean = prefs.getBoolean(KEY_COMPLETED, false)

    fun setCompleted(value: Boolean) {
        prefs.edit().putBoolean(KEY_COMPLETED, value).apply()
    }

    fun getGoals(): Set<String> = prefs.getStringSet(KEY_GOALS, emptySet()) ?: emptySet()

    fun setGoals(goals: Set<String>) {
        prefs.edit().putStringSet(KEY_GOALS, goals).apply()
    }

    fun getPreferredDuration(): Int = prefs.getInt(KEY_DURATION, 20)

    fun setPreferredDuration(minutes: Int) {
        prefs.edit().putInt(KEY_DURATION, minutes).apply()
    }

    fun getAnalyticsOptIn(): Boolean = prefs.getBoolean(KEY_ANALYTICS, false)

    fun setAnalyticsOptIn(value: Boolean) {
        prefs.edit().putBoolean(KEY_ANALYTICS, value).apply()
    }

    companion object {
        private const val PREFS_FILE = "fitness_tv_prefs"
        const val KEY_COMPLETED = "onboarding_completed"
        const val KEY_GOALS = "pref_goals"
        const val KEY_DURATION = "pref_duration"
        const val KEY_ANALYTICS = "analytics_opt_in"
    }
}
