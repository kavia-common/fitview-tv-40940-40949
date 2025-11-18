package com.example.fitness_tv_frontend.ui.widgets

import android.content.Context
import android.graphics.*
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.example.fitness_tv_frontend.R
import com.example.fitness_tv_frontend.model.ProgressEntry
import java.time.format.DateTimeFormatter
import kotlin.math.max

/**
 * Presenter rendering a mini bar chart for last 7 days of calories with dark styling.
 * - Bars in accent green
 * - Very subtle axis/labels for TV readability
 */
class ProgressChartPresenter(private val context: Context) : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val card = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(960, 240)
            titleText = "Weekly Calories"
            contentText = "Last 7 days"
            setInfoAreaBackgroundColor(Color.parseColor("#1A000000"))
            setBackgroundColor(ContextCompat.getColor(context, R.color.tv_surface))
        }
        return ViewHolder(card)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val list = item as List<*>
        val entries = list.filterIsInstance<ProgressEntry>()
        val width = 960
        val height = 240
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        // Background surface
        c.drawColor(ContextCompat.getColor(context, R.color.tv_surface))

        val maxCalories = max(1, entries.maxOfOrNull { it.calories } ?: 1)
        val barWidth = 50f
        val gap = 22f
        val leftPad = 40f
        val bottomPad = 40f
        val topPad = 22f

        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.accent_green)
        }
        val barBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.track_neutral)
        }
        val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#33FFFFFF")
            strokeWidth = 1.5f
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.tv_secondary)
            textSize = 18f
        }

        // Subtle baseline
        c.drawLine(leftPad, height - bottomPad, width.toFloat() - 20f, height - bottomPad, axisPaint)

        val df = DateTimeFormatter.ofPattern("E")
        entries.forEachIndexed { index, entry ->
            val x = leftPad + index * (barWidth + gap)
            val available = height - bottomPad - topPad
            val barHeight = (entry.calories.toFloat() / maxCalories) * available
            val top = height - bottomPad - barHeight
            // background column
            val bgRect = RectF(x, topPad + 6f, x + barWidth, height - bottomPad)
            c.drawRoundRect(bgRect, 12f, 12f, barBgPaint)
            // active bar
            val rect = RectF(x, top, x + barWidth, height - bottomPad)
            c.drawRoundRect(rect, 12f, 12f, barPaint)
            // labels
            c.drawText(entry.date.format(df), x, height - bottomPad + 18f, textPaint)
        }

        val card = viewHolder.view as ImageCardView
        card.mainImageView.setImageBitmap(bmp)
        val maxDescCal = max(1, entries.maxOfOrNull { it.calories } ?: 1)
        val minDescCal = entries.minOfOrNull { it.calories } ?: 0
        card.contentDescription = "Weekly calories bar chart for last seven days. Max $maxDescCal, min $minDescCal."
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val card = viewHolder.view as ImageCardView
        card.mainImage = null
    }
}
