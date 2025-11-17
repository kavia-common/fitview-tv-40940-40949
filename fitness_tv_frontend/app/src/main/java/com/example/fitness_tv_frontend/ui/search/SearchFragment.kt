package com.example.fitness_tv_frontend.ui.search

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import com.example.fitness_tv_frontend.data.MockRepository
import com.example.fitness_tv_frontend.databinding.DialogSearchBinding
import com.example.fitness_tv_frontend.ui.widgets.WorkoutCardPresenter
import java.util.Locale

/**
 * Dialog fragment providing voice search and text input filtering for workouts.
 */
class SearchFragment : DialogFragment() {

    private lateinit var binding: DialogSearchBinding
    private val repo by lazy { MockRepository(requireContext()) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSearchBinding.inflate(LayoutInflater.from(context))

        val resultsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val innerAdapter = ArrayObjectAdapter(WorkoutCardPresenter(requireContext()))
        resultsAdapter.add(ListRow(innerAdapter))

        fun updateResults(q: String) {
            innerAdapter.clear()
            repo.searchWorkouts(q).forEach { innerAdapter.add(it) }
        }

        binding.searchButton.setOnClickListener {
            val q = binding.searchEdit.text?.toString() ?: ""
            updateResults(q)
        }

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

        return AlertDialog.Builder(requireContext())
            .setTitle("Search")
            .setView(binding.root)
            .setPositiveButton("Close", null)
            .create()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2001) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val q = results?.firstOrNull().orEmpty()
            binding.searchEdit.setText(q)
        }
    }
}
