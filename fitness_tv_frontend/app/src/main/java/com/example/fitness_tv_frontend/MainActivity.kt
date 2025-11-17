package com.example.fitness_tv_frontend

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.FragmentActivity
import com.example.fitness_tv_frontend.ui.home.HomeFragment
import com.example.fitness_tv_frontend.ui.onboarding.OnboardingActivity
import com.example.fitness_tv_frontend.ui.onboarding.OnboardingPrefs

/**
 * Main Activity hosting the HomeFragment (Leanback Browse) for Android TV.
 * Provides single-activity navigation and TV remote handling.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Show Home by default
        if (supportFragmentManager.findFragmentByTag("home") == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, HomeFragment.newInstance(), "home")
                .commitNow()
        }

        // Launch onboarding if not completed
        val completed = OnboardingPrefs(this).isCompleted()
        if (!completed) {
            startActivity(OnboardingActivity.intent(this))
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Delegate to fragments when needed; keep BACK handling default
        return super.onKeyDown(keyCode, event)
    }

    // PUBLIC_INTERFACE
    fun openVoiceSearch() {
        /** Trigger the voice/search fragment (handled within HomeFragment UI). */
        // Keeping as placeholder in case activity-level search is needed later.
    }

    // PUBLIC_INTERFACE
    fun openSettings() {
        /** Opens the onboarding flow to reconfigure preferences. */
        startActivity(OnboardingActivity.intent(this))
    }
}
