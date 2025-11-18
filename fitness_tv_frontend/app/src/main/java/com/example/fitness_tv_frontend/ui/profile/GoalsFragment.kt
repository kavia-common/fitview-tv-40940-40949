package com.example.fitness_tv_frontend.ui.profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.app.AlertDialog
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.fitness_tv_frontend.data.MockRepository
import com.example.fitness_tv_frontend.databinding.DialogGoalsBinding
import com.example.fitness_tv_frontend.model.Goal

/**
 * Dialog fragment to edit fitness goals with validation and sensible defaults.
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
                val weekly = binding.weeklyWorkoutsEdit.text.toString().toIntOrNull()?.coerceIn(1, 14)
                    ?: current.weeklyWorkouts.coerceIn(1, 14)
                val targetCal = binding.targetCaloriesEdit.text.toString().toIntOrNull()?.coerceIn(200, 10000)
                    ?: current.targetCaloriesPerWeek.coerceIn(200, 10000)

                val updated = Goal(
                    weeklyWorkouts = weekly,
                    targetCaloriesPerWeek = targetCal
                )
                repo.updateGoal(updated)
                Toast.makeText(requireContext(), "Goals saved", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}
