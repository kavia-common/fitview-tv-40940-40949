package com.example.fitness_tv_frontend.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.GuidedStepSupportFragment
import com.example.fitness_tv_frontend.R

/**
 * Activity hosting the Onboarding flow using Leanback GuidedStepSupportFragment.
 * Appears on first launch or can be started from Home settings.
 */
class OnboardingActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep a simple full-screen container using the Activity's content; GuidedStep will handle UI.
        if (savedInstanceState == null) {
            GuidedStepSupportFragment.addAsRoot(
                this,
                WelcomeOnboardingFragment(),
                android.R.id.content
            )
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Default handling is fine; GuidedStep manages DPAD and BACK navigation between steps.
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        // PUBLIC_INTERFACE
        fun intent(context: Context): Intent {
            /** Returns an Intent to launch the onboarding flow. */
            return Intent(context, OnboardingActivity::class.java)
        }
    }
}
