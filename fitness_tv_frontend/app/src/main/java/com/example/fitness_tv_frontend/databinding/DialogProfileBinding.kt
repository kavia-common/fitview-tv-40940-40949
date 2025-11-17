package com.example.fitness_tv_frontend.databinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.example.fitness_tv_frontend.R

/**
 * Minimal binding for dialog_profile (manual binding to avoid adding DataBinding dependency).
 */
class DialogProfileBinding private constructor(val root: View) {
    val nameEdit: EditText = root.findViewById(R.id.name_edit)
    val ageEdit: EditText = root.findViewById(R.id.age_edit)
    val weightEdit: EditText = root.findViewById(R.id.weight_edit)
    val heightEdit: EditText = root.findViewById(R.id.height_edit)

    companion object {
        fun inflate(inflater: LayoutInflater, parent: ViewGroup? = null): DialogProfileBinding {
            val view = inflater.inflate(R.layout.dialog_profile, parent, false)
            return DialogProfileBinding(view)
        }
    }
}
