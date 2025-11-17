package com.example.fitness_tv_frontend.ui.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.example.fitness_tv_frontend.R
import com.example.fitness_tv_frontend.model.Workout

/**
 * Presenter for Workout media cards (16:9).
 * - Size: 320x180dp (1080p reference)
 * - Radius: 12dp
 * - Focus: scale 1.06, 3dp accent outline, elevation 12
 * - Subtitle line for meta, optional badge simulated in content text
 */
// PUBLIC_INTERFACE
class WorkoutCardPresenter(
    private val context: Context,
    private val onClick: ((Workout) -> Unit)? = null
) : Presenter() {

    private fun dp(context: Context, value: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.resources.displayMetrics).toInt()

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val w = parent.resources.getDimension(R.dimen.card_media_width_1080)
        val h = parent.resources.getDimension(R.dimen.card_media_height_1080)

        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(w.toInt(), h.toInt())
        }

        // Base rounded background (dark surface)
        val bg = GradientDrawable().apply {
            cornerRadius = parent.resources.getDimension(R.dimen.card_radius_standard)
            setColor(ContextCompat.getColor(context, R.color.tv_surface_2))
        }
        cardView.background = bg
        cardView.setInfoAreaBackgroundColor(Color.parseColor("#1A000000"))

        val accent = ContextCompat.getColor(context, R.color.tv_accent)
        val strokeWidthPx = dp(parent.context, 3f)
        val elevNormal = parent.resources.getDimension(R.dimen.card_elevation)
        val elevFocused = parent.resources.getDimension(R.dimen.card_elevation_focused)

        // Default elevation
        ViewCompat.setElevation(cardView, elevNormal)

        // Focus feedback: scale up and draw accent border + increase elevation
        cardView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            v.animate()
                .scaleX(if (hasFocus) 1.06f else 1f)
                .scaleY(if (hasFocus) 1.06f else 1f)
                .setDuration(140)
                .start()
            (cardView.background as? GradientDrawable)?.apply {
                if (hasFocus) {
                    setStroke(strokeWidthPx, accent)
                } else {
                    setStroke(0, Color.TRANSPARENT)
                }
            }
            ViewCompat.setElevation(cardView, if (hasFocus) elevFocused else elevNormal)
        }

        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val workout = item as Workout
        val cardView = viewHolder.view as ImageCardView
        cardView.titleText = workout.title

        // Simulate a small status/badge via meta prefix (e.g., ● Continue)
        val badge = when (workout.category.lowercase()) {
            "continue" -> "● Continue"
            "daily" -> "● Daily"
            "recommended" -> "● Recommended"
            else -> null
        }
        val meta = "${workout.durationMin} min • ${workout.intensity} • ${workout.caloriesEstimate} kcal"
        cardView.contentText = if (badge != null) "$badge  •  $meta" else meta

        if (workout.thumbnailUrl.isNotEmpty()) {
            Glide.with(context).load(workout.thumbnailUrl).into(cardView.mainImageView)
        } else {
            cardView.mainImageView.setImageResource(R.drawable.ic_placeholder_thumb)
        }

        // Accessibility
        cardView.contentDescription = "${workout.title}. $meta."

        // Optional click for contexts outside ListRow handler (e.g., Search dialog)
        cardView.setOnClickListener {
            // pressed micro-scale effect
            cardView.animate().scaleX(0.98f).scaleY(0.98f).setDuration(70).withEndAction {
                cardView.animate().scaleX(1.06f.takeIf { cardView.isFocused } ?: 1f)
                    .scaleY(1.06f.takeIf { cardView.isFocused } ?: 1f)
                    .setDuration(90).start()
            }.start()
            onClick?.invoke(workout)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val card = viewHolder.view as ImageCardView
        card.mainImage = null
        card.setOnClickListener(null)
        card.onFocusChangeListener = null
    }
}
