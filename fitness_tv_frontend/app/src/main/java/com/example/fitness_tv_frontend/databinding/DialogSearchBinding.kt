package com.example.fitness_tv_frontend.databinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.fitness_tv_frontend.R

/**
 * Minimal binding for dialog_search layout.
 */
class DialogSearchBinding private constructor(val root: View) {
    val searchEdit: EditText = root.findViewById(R.id.search_edit)
    val searchButton: Button = root.findViewById(R.id.search_button)
    val micButton: Button = root.findViewById(R.id.mic_button)

    companion object {
        fun inflate(inflater: LayoutInflater, parent: ViewGroup? = null): DialogSearchBinding {
            val view = inflater.inflate(R.layout.dialog_search, parent, false)
            return DialogSearchBinding(view)
        }
    }
}
