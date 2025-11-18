package com.example.fitness_tv_frontend.ui.profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.app.AlertDialog
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.fitness_tv_frontend.data.MockRepository
import com.example.fitness_tv_frontend.databinding.DialogProfileBinding
import com.example.fitness_tv_frontend.model.Profile

/**
 * Dialog fragment to edit user profile with basic validation and sensible defaults.
 */
class ProfileFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val repo = MockRepository(requireContext())
        val profile = repo.getProfile()
        val binding = DialogProfileBinding.inflate(LayoutInflater.from(requireContext()))

        binding.nameEdit.setText(profile.name)
        binding.ageEdit.setText(profile.age.toString())
        binding.weightEdit.setText(profile.weightKg.toString())
        binding.heightEdit.setText(profile.heightCm.toString())

        return AlertDialog.Builder(requireContext())
            .setTitle("Profile")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                val name = binding.nameEdit.text.toString().ifBlank { "You" }
                val age = binding.ageEdit.text.toString().toIntOrNull()?.coerceIn(10, 100) ?: profile.age
                val weight = binding.weightEdit.text.toString().toIntOrNull()?.coerceIn(30, 250) ?: profile.weightKg
                val height = binding.heightEdit.text.toString().toIntOrNull()?.coerceIn(120, 230) ?: profile.heightCm

                val updated = Profile(
                    name = name,
                    age = age,
                    weightKg = weight,
                    heightCm = height,
                    goal = profile.goal
                )
                repo.updateProfile(updated)
                Toast.makeText(requireContext(), "Profile saved", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}
