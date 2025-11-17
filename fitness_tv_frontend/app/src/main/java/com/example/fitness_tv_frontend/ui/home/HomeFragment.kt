package com.example.fitness_tv_frontend.ui.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup

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
import com.example.fitness_tv_frontend.ui.profile.ProfileFragment
import com.example.fitness_tv_frontend.ui.profile.GoalsFragment
import com.example.fitness_tv_frontend.ui.search.SearchFragment
import com.example.fitness_tv_frontend.ui.widgets.ProgressChartPresenter
import com.example.fitness_tv_frontend.ui.widgets.WorkoutCardPresenter

/**
 * Home screen fragment using BrowseSupportFragment with multiple rows.
 * Includes a top search affordance and DPAD navigation.
 */
class HomeFragment : BrowseSupportFragment() {

    private lateinit var repo: MockRepository
    private lateinit var rowsAdapter: ArrayObjectAdapter

    companion object {
        // PUBLIC_INTERFACE
        fun newInstance(): HomeFragment = HomeFragment()
        private const val ROW_DAILY = 0
        private const val ROW_RECOMMENDED = 1
        private const val ROW_PROGRESS = 2
        private const val ROW_BADGES = 3
        private const val ROW_PROFILE_GOALS = 4
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
        brandColor = ContextCompat.getColor(requireContext(), R.color.ocean_primary)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        badgeDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_app_badge, requireContext().theme)

        // Use Ocean secondary as the search orb accent to match the theme
        setSearchAffordanceColor(ContextCompat.getColor(requireContext(), R.color.ocean_secondary))

        setOnSearchClickedListener {
            openVoiceSearch()
        }
    }

    private fun buildRows() {
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = rowsAdapter

        // Row 0: Daily / Continue
        val dailyAdapter = ArrayObjectAdapter(WorkoutCardPresenter(requireContext()))
        repo.getDailyOrContinue().forEach { dailyAdapter.add(it) }
        rowsAdapter.add(ListRow(HeaderItem(ROW_DAILY.toLong(), "Daily / Continue Workout"), dailyAdapter))

        // Row 1: Recommended Workouts
        val recAdapter = ArrayObjectAdapter(WorkoutCardPresenter(requireContext()))
        repo.getRecommended(repo.getProfile()).forEach { recAdapter.add(it) }
        rowsAdapter.add(ListRow(HeaderItem(ROW_RECOMMENDED.toLong(), "Recommended Workouts"), recAdapter))

        // Row 2: Progress Charts (single card with chart)
        val chartAdapter = ArrayObjectAdapter(ProgressChartPresenter(requireContext()))
        chartAdapter.add(repo.recentProgress())
        rowsAdapter.add(ListRow(HeaderItem(ROW_PROGRESS.toLong(), "Progress (Last 7 days)"), chartAdapter))

        // Row 3: Motivational Badges
        val badgePresenter = object : Presenter() {
            override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
                val cardView = ImageCardView(parent.context).apply {
                    isFocusable = true
                    isFocusableInTouchMode = true
                    setMainImageDimensions(300, 200)
                }
                return ViewHolder(cardView)
            }

            override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
                val b = item as Badge
                val card = viewHolder.view as ImageCardView
                card.titleText = b.name
                card.contentText = b.description
                card.mainImage = null
                card.setInfoAreaBackgroundColor(Color.parseColor("#1AFFFFFF"))
                card.setBackgroundColor(Color.parseColor("#0DFFFFFF"))
                card.badgeImage = null
                // Use icon as content text prefix
                card.contentText = "${b.icon}  ${b.description}"
            }

            override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
        }
        val badgesAdapter = ArrayObjectAdapter(badgePresenter)
        repo.getBadges().forEach { badgesAdapter.add(it) }
        rowsAdapter.add(ListRow(HeaderItem(ROW_BADGES.toLong(), "Motivational Badges"), badgesAdapter))

        // Row 4: Profile & Goals
        val profileGoalsPresenter = object : Presenter() {
            override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
                val grid = ListRowPresenter()
                return ViewHolder(View(parent.context))
            }
            override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {}
            override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
        }
        val simplePresenter = StringPresenter()
        val pgAdapter = ArrayObjectAdapter(simplePresenter)
        pgAdapter.add("Profile")
        pgAdapter.add("Goals")
        rowsAdapter.add(ListRow(HeaderItem(ROW_PROFILE_GOALS.toLong(), "Profile & Goals"), pgAdapter))
    }

    private fun setupEventListeners() {
        setOnItemViewClickedListener { _, item, _, row ->
            when (item) {
                is Workout -> {
                    startActivity(
                        Intent(requireContext(), PlayerActivity::class.java).apply {
                            putExtra(PlayerActivity.EXTRA_WORKOUT, item)
                        }
                    )
                }
                is ListRow -> {} // no-op
                is String -> {
                    when (item) {
                        "Profile" -> openProfile()
                        "Goals" -> openGoals()
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

    class StringPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            val card = ImageCardView(parent.context).apply {
                isFocusable = true
                isFocusableInTouchMode = true
                titleText = "Open"
                setMainImageDimensions(280, 160)
                setBackgroundColor(Color.parseColor("#102563EB"))
                setInfoAreaBackgroundColor(Color.parseColor("#1AFFFFFF"))
            }
            return ViewHolder(card)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
            val card = viewHolder.view as ImageCardView
            val label = item as String
            card.titleText = label
            card.contentText = if (label == "Profile") "Edit your profile" else "Set your goals"
        }

        override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
    }
}
