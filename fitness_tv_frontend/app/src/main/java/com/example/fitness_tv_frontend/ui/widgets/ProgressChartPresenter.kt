package com.example.fitness_tv_frontend.ui.widgets

import android.content.Context
import android.graphics.*
import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.example.fitness_tv_frontend.R
import com.example.fitness_tv_frontend.model.ProgressEntry
import java.time.format.DateTimeFormatter
import kotlin.math.max

/**
 * Presenter rendering a simple bar chart for last 7 days of calories.
 */
class ProgressChartPresenter(private val context: Context) : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val card = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(900, 260)
            titleText = "Weekly Calories"
            contentText = "Last 7 days"
        }
        return ViewHolder(card)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val list = item as List<*>
        val entries = list.filterIsInstance<ProgressEntry>()
        val width = 900
        val height = 260
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.parseColor("#0F1B2B"))

        val maxCal = max(1, entries.maxOfOrNull { it.calories } ?: 1)
        val barWidth = 90f
        val gap = 35f
        val leftPad = 40f
        val bottomPad = 40f

        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = androidx.core.content.ContextCompat.getColor(context, R.color.ocean_secondary)
        }
        val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#55FFFFFF")
            strokeWidth = 2f
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 22f
        }

        // Axes
        c.drawLine(leftPad, height - bottomPad, width.toFloat() - 20f, height - bottomPad, axisPaint)

        val df = DateTimeFormatter.ofPattern("E")
        entries.forEachIndexed { index, entry ->
            val x = leftPad + index * (barWidth + gap)
            val barHeight = (entry.calories.toFloat() / maxCal) * (height - 2 * bottomPad)
            val top = height - bottomPad - barHeight
            val rect = RectF(x, top, x + barWidth, height - bottomPad)
            c.drawRoundRect(rect, 16f, 16f, barPaint)
            c.drawText(entry.date.format(df), x, height - 10f, textPaint)
        }

        val card = viewHolder.view as ImageCardView
        card.mainImageView.setImageBitmap(bmp)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val card = viewHolder.view as ImageCardView
        card.mainImage = null
    }
}
