package com.example.fitness_tv_frontend

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitness_tv_frontend.data.MockRepository
import com.example.fitness_tv_frontend.model.Goal
import com.example.fitness_tv_frontend.model.Profile
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for recommendation filtering logic that require Android Context/SharedPreferences.
 * Verifies:
 * - Recommendations bias by goals and preferred duration
 * - Results are never empty (fallback behavior)
 */
@RunWith(AndroidJUnit4::class)
class RecommendationLogicInstrumentedTest {

    private lateinit var context: Context
    private lateinit var repo: MockRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear prefs to deterministic state
        val prefs = context.getSharedPreferences("fitness_tv_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        repo = MockRepository(context)
    }

    @Test
    fun recommended_biasesByGoals_highIntensity_whenWeightLossSelected() {
        val prefs = context.getSharedPreferences("fitness_tv_prefs", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("pref_goals", setOf("weight_loss")).apply()
        prefs.edit().putInt("pref_duration", 20).apply()

        val profile = Profile("Test", 30, 70, 170, Goal(weeklyWorkouts = 5, targetCaloriesPerWeek = 1200))
        val recs = repo.getRecommended(profile)

        assertTrue("Recommendations should not be empty", recs.isNotEmpty())
        val topIsHigh = recs.take(3).any { it.intensity == "High" }
        assertTrue("Top recommendations should include High intensity due to weight_loss goal", topIsHigh)
    }

    @Test
    fun recommended_prefersDurationNearPreference() {
        val prefs = context.getSharedPreferences("fitness_tv_prefs", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("pref_goals", setOf("flexibility")).apply()
        prefs.edit().putInt("pref_duration", 10).apply()

        val profile = Profile("Test", 30, 70, 170, Goal(weeklyWorkouts = 2, targetCaloriesPerWeek = 800))
        val recs = repo.getRecommended(profile)

        assertTrue("Recommendations should not be empty", recs.isNotEmpty())
        val hasCloseDurationEarly = recs.take(4).any { kotlin.math.abs(it.durationMin - 10) <= 7 }
        assertTrue("Recommendations should bias toward preferred duration near 10", hasCloseDurationEarly)
    }

    @Test
    fun recommended_neverEmpty_evenWhenNoGoals() {
        val prefs = context.getSharedPreferences("fitness_tv_prefs", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("pref_goals", emptySet()).apply()
        prefs.edit().putInt("pref_duration", 99).apply()

        val profile = Profile("T", 20, 70, 170, Goal(weeklyWorkouts = 1, targetCaloriesPerWeek = 500))
        val recs = repo.getRecommended(profile)

        assertTrue("Recommendations must never be empty due to fallback path", recs.isNotEmpty())
        val allKnown = recs.all { it.category.isNotEmpty() && it.title.isNotEmpty() }
        assertTrue("Returned items should be valid workouts", allKnown)
    }

    @Test
    fun recommended_respectsIntensityBias_strengthGivesMedium() {
        val prefs = context.getSharedPreferences("fitness_tv_prefs", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("pref_goals", setOf("strength")).apply()
        prefs.edit().putInt("pref_duration", 20).apply()

        val profile = Profile("T", 20, 70, 170, Goal(weeklyWorkouts = 3, targetCaloriesPerWeek = 900))
        val recs = repo.getRecommended(profile)

        assertTrue("Recommendations should not be empty", recs.isNotEmpty())
        val hasMediumInTop = recs.take(3).any { it.intensity == "Medium" }
        assertTrue("Strength goal should bias toward Medium intensity in top results", hasMediumInTop)
    }

    @Test
    fun recommended_containsRecommendedCategoryOrBias() {
        val prefs = context.getSharedPreferences("fitness_tv_prefs", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("pref_goals", setOf("cardio")).apply()
        prefs.edit().putInt("pref_duration", 20).apply()

        val profile = Profile("T", 20, 70, 170, Goal(weeklyWorkouts = 6, targetCaloriesPerWeek = 1600))
        val recs = repo.getRecommended(profile)

        assertTrue(recs.isNotEmpty())
        val allFiltered = recs.all { it.category == "Recommended" || it.intensity == "High" }
        assertTrue("Filtering should keep Recommended category or items matching the intensity bias", allFiltered)
    }
}
