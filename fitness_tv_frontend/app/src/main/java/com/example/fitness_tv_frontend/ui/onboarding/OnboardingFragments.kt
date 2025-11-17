package com.example.fitness_tv_frontend.ui.onboarding

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import com.example.fitness_tv_frontend.R

/**
 * Welcome screen: brief app overview.
 */
class WelcomeOnboardingFragment : GuidedStepSupportFragment() {

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        val title = getString(R.string.onboarding_welcome_title)
        val description = getString(R.string.onboarding_welcome_desc)
        return GuidanceStylist.Guidance(title, description, null, null)
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        actions += GuidedAction.Builder(context)
            .id(ACTION_NEXT)
            .title(getString(R.string.onboarding_next))
            .build()
        actions += GuidedAction.Builder(context)
            .id(ACTION_SKIP)
            .title(getString(R.string.onboarding_skip))
            .description(getString(R.string.onboarding_skip_desc))
            .build()
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            ACTION_NEXT -> {
                GuidedStepSupportFragment.add(
                    parentFragmentManager,
                    GoalsOnboardingFragment()
                )
            }
            ACTION_SKIP -> {
                // Mark completed with defaults
                val prefs = OnboardingPrefs(requireContext())
                prefs.setCompleted(true)
                // Defaults: no goals, 20min, analytics false
                prefs.setGoals(emptySet())
                prefs.setPreferredDuration(20)
                prefs.setAnalyticsOptIn(false)
                requireActivity().finish()
            }
        }
    }

    companion object {
        private const val ACTION_NEXT = 1L
        private const val ACTION_SKIP = 2L
    }
}

/**
 * Goals selection screen: multi-selection of goals.
 */
class GoalsOnboardingFragment : GuidedStepSupportFragment() {

    private val goals = listOf(
        GoalItem("weight_loss", R.string.goal_weight_loss),
        GoalItem("strength", R.string.goal_strength),
        GoalItem("flexibility", R.string.goal_flexibility),
        GoalItem("cardio", R.string.goal_cardio),
    )

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        return GuidanceStylist.Guidance(
            getString(R.string.onboarding_goals_title),
            getString(R.string.onboarding_goals_desc),
            null,
            null
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        val prefs = OnboardingPrefs(requireContext())
        val selected = prefs.getGoals()

        // Checkboxes for each goal
        for (g in goals) {
            actions += GuidedAction.Builder(context)
                .id(g.id.hashCode().toLong())
                .title(getString(g.labelRes))
                .checkSetId(GuidedAction.CHECKBOX_CHECK_SET_ID)
                .checked(selected.contains(g.id))
                .build()
        }

        // Continue
        actions += GuidedAction.Builder(context)
            .id(ACTION_CONTINUE)
            .title(getString(R.string.onboarding_continue))
            .build()
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        if (action.id == ACTION_CONTINUE) {
            // Collect checked goals from current actions
            val chosen = getActions()
                .filter { it.checkSetId == GuidedAction.CHECKBOX_CHECK_SET_ID && it.isChecked }
                .map { actionToGoalId(it) }
                .toSet()

            val prefs = OnboardingPrefs(requireContext())
            prefs.setGoals(chosen)

            GuidedStepSupportFragment.add(
                parentFragmentManager,
                DurationOnboardingFragment()
            )
        } else {
            // Toggle handled by framework; no-op
        }
    }

    private fun actionToGoalId(action: GuidedAction): String {
        // Map title back to goal id using labels
        val label = action.title?.toString().orEmpty()
        val matched = goals.firstOrNull { getString(it.labelRes) == label }
        return matched?.id ?: label.lowercase()
    }

    companion object {
        private const val ACTION_CONTINUE = 1000L
        data class GoalItem(val id: String, val labelRes: Int)
    }
}

/**
 * Duration selection screen: radio options for preferred workout duration.
 */
class DurationOnboardingFragment : GuidedStepSupportFragment() {

    private val durations = listOf(10, 20, 30, 45)

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        return GuidanceStylist.Guidance(
            getString(R.string.onboarding_duration_title),
            getString(R.string.onboarding_duration_desc),
            null,
            null
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        val prefs = OnboardingPrefs(requireContext())
        val current = prefs.getPreferredDuration()

        durations.forEach { minutes ->
            actions += GuidedAction.Builder(context)
                .id(minutes.toLong())
                .title(getString(R.string.onboarding_duration_option, minutes))
                .checkSetId(GuidedAction.DEFAULT_CHECK_SET_ID)
                .checked(current == minutes)
                .build()
        }

        actions += GuidedAction.Builder(context)
            .id(ACTION_CONTINUE)
            .title(getString(R.string.onboarding_continue))
            .build()
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        if (action.id == ACTION_CONTINUE) {
            // Find selected radio option
            val selected = getActions().firstOrNull {
                it.checkSetId == GuidedAction.DEFAULT_CHECK_SET_ID && it.isChecked
            }?.id?.toInt() ?: durations[1]

            val prefs = OnboardingPrefs(requireContext())
            prefs.setPreferredDuration(selected)

            GuidedStepSupportFragment.add(
                parentFragmentManager,
                AnalyticsOnboardingFragment()
            )
        } else {
            // Toggle handled by framework
        }
    }

    companion object {
        private const val ACTION_CONTINUE = 2000L
    }
}

/**
 * Analytics consent screen: optional opt-in.
 */
class AnalyticsOnboardingFragment : GuidedStepSupportFragment() {

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        return GuidanceStylist.Guidance(
            getString(R.string.onboarding_analytics_title),
            getString(R.string.onboarding_analytics_desc),
            null,
            null
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        val prefs = OnboardingPrefs(requireContext())
        val checked = prefs.getAnalyticsOptIn()
        actions += GuidedAction.Builder(context)
            .id(ACTION_OPT_IN)
            .title(getString(R.string.onboarding_analytics_opt_in))
            .checkSetId(GuidedAction.CHECKBOX_CHECK_SET_ID)
            .checked(checked)
            .build()

        actions += GuidedAction.Builder(context)
            .id(ACTION_FINISH)
            .title(getString(R.string.onboarding_finish))
            .build()
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            ACTION_OPT_IN -> {
                // Toggle is handled by framework; nothing else to do right now.
            }
            ACTION_FINISH -> {
                val opted = getActions().firstOrNull { it.id == ACTION_OPT_IN }?.isChecked ?: false
                val prefs = OnboardingPrefs(requireContext())
                prefs.setAnalyticsOptIn(opted)
                GuidedStepSupportFragment.add(
                    parentFragmentManager,
                    FinishOnboardingFragment()
                )
            }
        }
    }

    companion object {
        private const val ACTION_OPT_IN = 3000L
        private const val ACTION_FINISH = 3001L
    }
}

/**
 * Finish screen: sets completion flag and returns to Home.
 */
class FinishOnboardingFragment : GuidedStepSupportFragment() {
    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        return GuidanceStylist.Guidance(
            getString(R.string.onboarding_done_title),
            getString(R.string.onboarding_done_desc),
            null,
            null
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        actions += GuidedAction.Builder(context)
            .id(ACTION_START)
            .title(getString(R.string.onboarding_start_using_app))
            .build()
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        if (action.id == ACTION_START) {
            val prefs = OnboardingPrefs(requireContext())
            prefs.setCompleted(true)
            requireActivity().finish()
        }
    }

    companion object {
        private const val ACTION_START = 4000L
    }
}
