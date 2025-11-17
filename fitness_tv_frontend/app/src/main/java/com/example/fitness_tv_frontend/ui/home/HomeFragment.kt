package com.example.fitness_tv_frontend.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.Presenter
import com.example.fitness_tv_frontend.R
import com.example.fitness_tv_frontend.data.MockRepository
import com.example.fitness_tv_frontend.model.Badge
import com.example.fitness_tv_frontend.model.Workout
import com.example.fitness_tv_frontend.ui.player.PlayerActivity
import com.example.fitness_tv_frontend.ui.profile.GoalsFragment
import com.example.fitness_tv_frontend.ui.profile.ProfileFragment
import com.example.fitness_tv_frontend.ui.search.SearchFragment
import com.example.fitness_tv_frontend.ui.widgets.HeroMetricsPresenter
import com.example.fitness_tv_frontend.ui.widgets.ProgressChartPresenter
import com.example.fitness_tv_frontend.ui.widgets.WorkoutCardPresenter

/**
 * Home screen fragment using BrowseSupportFragment with hero metrics & rows.
 * - Top hero: ring gauges + mini bar
 * - Rails: Daily/Continue, Recommended, Progress, Badges, Profile & Goals
 * - DPAD friendly with clear focus and accessibility labels
 */
class HomeFragment : BrowseSupportFragment() {

    private lateinit var repo: MockRepository
    private lateinit var rowsAdapter: ArrayObjectAdapter

    companion object {
        // PUBLIC_INTERFACE
        fun newInstance(): HomeFragment = HomeFragment()

        private const val ROW_HERO = 0
        private const val ROW_DAILY = 1
        private const val ROW_RECOMMENDED = 2
        private const val ROW_PROGRESS = 3
        private const val ROW_BADGES = 4
        private const val ROW_PROFILE_GOALS = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = MockRepository(requireContext())
        setupUi()
        buildRows()
        setupEventListeners()
    }

    private fun setupUi() {
        title = resources.getString(R.string.app_name)
        // Accent and header styling per dark theme
        brandColor = ContextCompat.getColor(requireContext(), R.color.tv_accent)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        badgeDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_app_badge, requireContext().theme)

        // Green accent on search orb to match theme
        setSearchAffordanceColor(ContextCompat.getColor(requireContext(), R.color.tv_accent))

        setOnSearchClickedListener {
            openVoiceSearch()
        }
    }

    private fun buildRows() {
        // Increase row shadow to emphasize rails
        val listRowPresenter = ListRowPresenter().apply {
            shadowEnabled = true
            selectEffectEnabled = true
        }
        rowsAdapter = ArrayObjectAdapter(listRowPresenter)
        adapter = rowsAdapter

        // Row 0: Hero metrics/summary
        val heroAdapter = ArrayObjectAdapter(HeroMetricsPresenter(requireContext()))
        val profile = repo.getProfile()
        val heroData = HeroMetricsPresenter.HeroData(
            activePercent = 92,
            alerts = 3,
            energyPercent = 68,
            recent = repo.recentProgress()
        )
        heroAdapter.add(heroData)
        rowsAdapter.add(ListRow(HeaderItem(ROW_HERO.toLong(), "Dashboard"), heroAdapter))

        // Row 1: Daily / Continue
        val dailyAdapter = ArrayObjectAdapter(WorkoutCardPresenter(requireContext()))
        repo.getDailyOrContinue().forEach { dailyAdapter.add(it) }
        rowsAdapter.add(ListRow(HeaderItem(ROW_DAILY.toLong(), "Daily & Continue"), dailyAdapter))

        // Row 2: Recommended Workouts
        val recAdapter = ArrayObjectAdapter(WorkoutCardPresenter(requireContext()))
        repo.getRecommended(profile).forEach { recAdapter.add(it) }
        rowsAdapter.add(ListRow(HeaderItem(ROW_RECOMMENDED.toLong(), "Recommended For You"), recAdapter))

        // Row 3: Progress Charts (single card with chart)
        val chartAdapter = ArrayObjectAdapter(ProgressChartPresenter(requireContext()))
        chartAdapter.add(repo.recentProgress())
        rowsAdapter.add(ListRow(HeaderItem(ROW_PROGRESS.toLong(), "Progress (Last 7 days)"), chartAdapter))

        // Row 4: Motivational Badges
        val badgePresenter = object : Presenter() {
            override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
                val cardView = ImageCardView(parent.context).apply {
                    isFocusable = true
                    isFocusableInTouchMode = true
                    setMainImageDimensions(300, 200)
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tv_surface_2))
                }
                return ViewHolder(cardView)
            }

            override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
                val b = item as Badge
                val card = viewHolder.view as ImageCardView
                card.titleText = b.name
                card.contentText = "${b.icon}  ${b.description}"
                card.badgeImage = null
                card.contentDescription = "Badge ${b.name}. ${b.description}."
            }

            override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
        }
        val badgesAdapter = ArrayObjectAdapter(badgePresenter)
        repo.getBadges().forEach { badgesAdapter.add(it) }
        rowsAdapter.add(ListRow(HeaderItem(ROW_BADGES.toLong(), "Motivational Badges"), badgesAdapter))

        // Row 5: Profile & Goals shortcuts
        val pgAdapter = ArrayObjectAdapter(StringPresenter())
        pgAdapter.add("Profile")
        pgAdapter.add("Goals")
        pgAdapter.add("Setup")
        rowsAdapter.add(ListRow(HeaderItem(ROW_PROFILE_GOALS.toLong(), "Profile & Goals"), pgAdapter))
    }

    private fun setupEventListeners() {
        setOnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is Workout -> {
                    startActivity(
                        Intent(requireContext(), PlayerActivity::class.java).apply {
                            putExtra(PlayerActivity.EXTRA_WORKOUT, item)
                        }
                    )
                }
                is ListRow -> { /* no-op */ }
                is String -> {
                    when (item) {
                        "Profile" -> openProfile()
                        "Goals" -> openGoals()
                        "Setup" -> openOnboarding()
                    }
                }
            }
        }
    }

    private fun openProfile() {
        val fm = parentFragmentManager
        ProfileFragment().show(fm, "profile")
    }

    private fun openGoals() {
        val fm = parentFragmentManager
        GoalsFragment().show(fm, "goals")
    }

    private fun openVoiceSearch() {
        val fm = parentFragmentManager
        SearchFragment().show(fm, "search")
    }

    private fun openOnboarding() {
        val ctx = requireContext()
        val intent = com.example.fitness_tv_frontend.ui.onboarding.OnboardingActivity.intent(ctx)
        startActivity(intent)
    }

    class StringPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            val card = ImageCardView(parent.context).apply {
                isFocusable = true
                isFocusableInTouchMode = true
                titleText = "Open"
                setMainImageDimensions(280, 160)
                setBackgroundColor(ContextCompat.getColor(parent.context, R.color.tv_surface_2))
                setInfoAreaBackgroundColor(Color.parseColor("#1A000000"))
            }
            return ViewHolder(card)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
            val card = viewHolder.view as ImageCardView
            val label = item as String
            card.titleText = label
            card.contentText = when (label) {
                "Profile" -> "Edit your profile"
                "Goals" -> "Set your goals"
                "Setup" -> "Run onboarding again"
                else -> "Open"
            }
            card.contentDescription = "$label. ${card.contentText}"
        }

        override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
    }
}
