package com.example.fitness_tv_frontend.ui.profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.fitness_tv_frontend.data.MockRepository
import com.example.fitness_tv_frontend.databinding.DialogProfileBinding
import com.example.fitness_tv_frontend.model.Profile
import com.example.fitness_tv_frontend.model.Goal

/**
 * Dialog fragment to edit user profile.
 */
class ProfileFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val repo = MockRepository(requireContext())
        val profile = repo.getProfile()
        val binding = DialogProfileBinding.inflate(LayoutInflater.from(context))

        binding.nameEdit.setText(profile.name)
        binding.ageEdit.setText(profile.age.toString())
        binding.weightEdit.setText(profile.weightKg.toString())
        binding.heightEdit.setText(profile.heightCm.toString())

        return AlertDialog.Builder(requireContext())
            .setTitle("Profile")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                val updated = Profile(
                    name = binding.nameEdit.text.toString().ifBlank { "You" },
                    age = binding.ageEdit.text.toString().toIntOrNull() ?: profile.age,
                    weightKg = binding.weightEdit.text.toString().toIntOrNull() ?: profile.weightKg,
                    heightCm = binding.heightEdit.text.toString().toIntOrNull() ?: profile.heightCm,
                    goal = profile.goal
                )
                repo.updateProfile(updated)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}
