package com.example.fitness_tv_frontend.ui.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.example.fitness_tv_frontend.R
import com.example.fitness_tv_frontend.model.Workout

/**
 * Presenter for Workout cards with rounded corners, Ocean theme focus border and subtle scale on focus.
 * Accepts an optional onClick callback for launching details/playback when used outside ListRow click handlers.
 */
class WorkoutCardPresenter(
    private val context: Context,
    private val onClick: ((Workout) -> Unit)? = null
) : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(360, 202)
        }

        // Base background with rounded corners
        val bg = GradientDrawable().apply {
            cornerRadius = parent.resources.getDimension(R.dimen.card_radius_large)
            // subtle dark surface for card
            setColor(Color.parseColor("#141F2E"))
        }
        cardView.background = bg
        cardView.setInfoAreaBackgroundColor(Color.parseColor("#1AFFFFFF"))

        val accent = ContextCompat.getColor(context, R.color.ocean_primary)
        val strokeWidthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3f,
            parent.resources.displayMetrics
        ).toInt()

        // Focus feedback: scale up slightly and draw accent border
        cardView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            v.animate().scaleX(if (hasFocus) 1.08f else 1f)
                .scaleY(if (hasFocus) 1.08f else 1f)
                .setDuration(150)
                .start()
            (cardView.background as? GradientDrawable)?.apply {
                if (hasFocus) {
                    setStroke(strokeWidthPx, accent)
                } else {
                    setStroke(0, Color.TRANSPARENT)
                }
            }
        }

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
        cardView.contentDescription = workout.title

        // Optional click for flows that don't use Fragment-level OnItemViewClickedListener (e.g., Search dialog)
        cardView.setOnClickListener {
            onClick?.invoke(workout)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val card = viewHolder.view as ImageCardView
        card.mainImage = null
        card.setOnClickListener(null)
        card.setOnFocusChangeListener(null)
    }
}
