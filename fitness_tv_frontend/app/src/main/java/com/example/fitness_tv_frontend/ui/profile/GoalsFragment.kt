package com.example.fitness_tv_frontend.ui.profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.fitness_tv_frontend.data.MockRepository
import com.example.fitness_tv_frontend.databinding.DialogGoalsBinding
import com.example.fitness_tv_frontend.model.Goal

/**
 * Dialog fragment to edit fitness goals.
 */
class GoalsFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val repo = MockRepository(requireContext())
        val current = repo.getProfile().goal
        val binding = DialogGoalsBinding.inflate(LayoutInflater.from(requireContext()))

        binding.weeklyWorkoutsEdit.setText(current.weeklyWorkouts.toString())
        binding.targetCaloriesEdit.setText(current.targetCaloriesPerWeek.toString())

        return AlertDialog.Builder(requireContext())
            .setTitle("Goals")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                val updated = Goal(
                    weeklyWorkouts = binding.weeklyWorkoutsEdit.text.toString().toIntOrNull() ?: current.weeklyWorkouts,
                    targetCaloriesPerWeek = binding.targetCaloriesEdit.text.toString().toIntOrNull() ?: current.targetCaloriesPerWeek
                )
                repo.updateGoal(updated)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}
