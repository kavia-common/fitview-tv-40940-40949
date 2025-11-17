package com.example.fitness_tv_frontend.databinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.example.fitness_tv_frontend.R

/**
 * Minimal binding for dialog_goals layout.
 */
class DialogGoalsBinding private constructor(val root: View) {
    val weeklyWorkoutsEdit: EditText = root.findViewById(R.id.weekly_workouts_edit)
    val targetCaloriesEdit: EditText = root.findViewById(R.id.target_calories_edit)

    companion object {
        fun inflate(inflater: LayoutInflater, parent: ViewGroup? = null): DialogGoalsBinding {
            val view = inflater.inflate(R.layout.dialog_goals, parent, false)
            return DialogGoalsBinding(view)
        }
    }
}
