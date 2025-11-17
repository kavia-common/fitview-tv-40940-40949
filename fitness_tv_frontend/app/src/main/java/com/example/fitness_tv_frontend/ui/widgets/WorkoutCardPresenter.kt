package com.example.fitness_tv_frontend.ui.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.example.fitness_tv_frontend.R
import com.example.fitness_tv_frontend.model.Workout

/**
 * Presenter for Workout cards with rounded corners and subtle shadows.
 */
class WorkoutCardPresenter(private val context: Context) : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        cardView.setMainImageDimensions(360, 202)

        val bg = GradientDrawable().apply {
            cornerRadius = parent.resources.getDimension(R.dimen.card_radius_large)
            setColor(Color.parseColor("#141F2E"))
        }
        cardView.background = bg
        cardView.setInfoAreaBackgroundColor(Color.parseColor("#1AFFFFFF"))
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val workout = item as Workout
        val cardView = viewHolder.view as ImageCardView
        cardView.titleText = workout.title
        cardView.contentText = "${workout.durationMin} min • ${workout.intensity} • ${workout.caloriesEstimate} kcal"
        cardView.badgeImage = null
        if (workout.thumbnailUrl.isNotEmpty()) {
            Glide.with(context).load(workout.thumbnailUrl).into(cardView.mainImageView)
        } else {
            cardView.mainImageView.setImageResource(R.drawable.ic_placeholder_thumb)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val card = viewHolder.view as ImageCardView
        card.mainImage = null
    }
}
