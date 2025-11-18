package com.example.fitness_tv_frontend.ui.search

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ItemBridgeAdapter
import com.example.fitness_tv_frontend.data.MockRepository
import com.example.fitness_tv_frontend.databinding.DialogSearchBinding
import com.example.fitness_tv_frontend.model.Workout
import com.example.fitness_tv_frontend.ui.player.PlayerActivity
import com.example.fitness_tv_frontend.ui.widgets.WorkoutCardPresenter
import java.util.Locale

/**
 * Dialog fragment providing voice search and text input filtering for workouts.
 * Renders results using a Leanback HorizontalGridView bound via ItemBridgeAdapter.
 */
class SearchFragment : DialogFragment() {

    private lateinit var binding: DialogSearchBinding
    private val repo by lazy { MockRepository(requireContext()) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSearchBinding.inflate(LayoutInflater.from(requireContext()))

        // Results adapter using Workout cards with click -> PlayerActivity
        val resultsAdapter = ArrayObjectAdapter(
            WorkoutCardPresenter(requireContext()) { workout ->
                launchPlayer(workout)
            }
        )
        val bridgeAdapter = ItemBridgeAdapter(resultsAdapter)
        binding.resultsGrid.adapter = bridgeAdapter

        fun updateResults(q: String) {
            resultsAdapter.clear()
            repo.searchWorkouts(q).forEach { resultsAdapter.add(it) }
            // ensure focus moves to results on TV after search
            if (resultsAdapter.size() > 0) {
                binding.resultsGrid.requestFocus()
            }
        }

        // Button click search
        binding.searchButton.setOnClickListener {
            val q = binding.searchEdit.text?.toString().orEmpty()
            updateResults(q)
        }

        // IME "Search" on keyboard enter
        binding.searchEdit.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP)
            ) {
                val q = binding.searchEdit.text?.toString().orEmpty()
                updateResults(q)
                true
            } else {
                false
            }
        }

        // Mic/voice search
        binding.micButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak workout name or intensity")
            }
            try {
                startActivityForResult(intent, 2001)
            } catch (_: ActivityNotFoundException) {
                // No speech recognizer available; ignore
            }
        }

        // Start with all results for discoverability
        updateResults("")

        return AlertDialog.Builder(requireContext())
            .setTitle("Search")
            .setView(binding.root)
            .setPositiveButton("Close", null)
            .create()
    }

    private fun launchPlayer(workout: Workout) {
        startActivity(
            Intent(requireContext(), PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_WORKOUT, workout)
            }
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2001) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val q = results?.firstOrNull().orEmpty()
            binding.searchEdit.setText(q)
            // auto-run the search with recognized query for TV convenience
            val text = binding.searchEdit.text?.toString().orEmpty()
            if (text.isNotEmpty()) {
                // Trigger search button programmatically
                binding.searchButton.performClick()
            }
        }
    }
}
