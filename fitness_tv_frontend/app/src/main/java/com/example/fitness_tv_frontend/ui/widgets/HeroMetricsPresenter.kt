package com.example.fitness_tv_frontend.ui.widgets

import android.content.Context
import android.graphics.*
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.example.fitness_tv_frontend.R
import com.example.fitness_tv_frontend.model.ProgressEntry
import kotlin.math.min

/**
 * HeroMetricsPresenter draws a composite hero card:
 * - Three circular ring gauges (e.g., Active %, Alerts, Energy)
 * - A mini bar strip underneath
 *
 * Item model: HeroData(val activePercent: Int, val alerts: Int, val energyPercent: Int, val recent: List<ProgressEntry>)
 */
class HeroMetricsPresenter(private val context: Context) : Presenter() {

    data class HeroData(
        val activePercent: Int,
        val alerts: Int,
        val energyPercent: Int,
        val recent: List<ProgressEntry>
    )

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val card = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            // Size approximates hero panel per notes
            setMainImageDimensions(1200, 360)
            titleText = "Today's Overview"
            contentText = "Summary metrics and recent activity"
            setInfoAreaBackgroundColor(Color.parseColor("#1AFFFFFF"))
            // Surface background to stand out on black canvas
            setBackgroundColor(ContextCompat.getColor(context, R.color.tv_surface))
        }
        return ViewHolder(card)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val data = item as HeroData
        val width = 1200
        val height = 360
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        // Card background fill
        c.drawColor(ContextCompat.getColor(context, R.color.tv_surface))

        // Base paints
        val track = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.track_neutral)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 22f
        }
        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.accent_green)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 22f
        }
        val danger = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.status_danger)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 22f
        }
        val info = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.status_info)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 22f
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 20f
        }
        val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.tv_secondary)
            textSize = 18f
        }

        // Padding
        val padH = 32f
        val padV = 24f

        // Title
        c.drawText("Hello!", padH, padV + 32f, titlePaint)
        c.drawText("Your dashboard at a glance", padH, padV + 60f, mutedPaint)

        // Ring gauges geometry
        val diameter = 176f
        val stroke = 22f
        val topY = padV + 80f
        val firstX = padH + 40f
        val gap = 40f + diameter

        fun drawRing(centerX: Float, centerY: Float, percent: Int, label: String, paint: Paint) {
            val radius = diameter / 2f
            val rect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
            track.strokeWidth = stroke
            paint.strokeWidth = stroke
            c.drawArc(rect, -90f, 360f, false, track)
            c.drawArc(rect, -90f, 360f * (percent / 100f), false, paint)

            // Center value
            val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 40f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val labelP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ContextCompat.getColor(context, R.color.tv_secondary)
                textSize = 20f
                textAlign = Paint.Align.CENTER
            }
            c.drawText("${percent}%", centerX, centerY + 12f, valuePaint)
            c.drawText(label, centerX, centerY + 40f, labelP)
        }

        // Three gauges: Active (green), Alerts (red inversed scale), Energy (blue)
        drawRing(firstX + diameter / 2, topY + diameter / 2, data.activePercent, "Active", accent)
        val alertsPercent = min(100, data.alerts * 10) // visualize count as percentage range
        drawRing(firstX + gap + diameter / 2, topY + diameter / 2, 100 - alertsPercent, "Alerts", danger)
        drawRing(firstX + gap * 2 + diameter / 2, topY + diameter / 2, data.energyPercent, "Energy", info)

        // Mini bar strip
        val miniTop = topY + diameter + 54f
        val miniHeight = 110f
        val miniLeft = padH + 16f
        val miniRight = width - padH - 16f

        val barBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.track_neutral)
        }
        val barFg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.accent_green)
        }

        // Background strip
        val bgRect = RectF(miniLeft, miniTop, miniRight, miniTop + miniHeight)
        c.drawRoundRect(bgRect, 16f, 16f, Paint().apply { color = ContextCompat.getColor(context, R.color.tv_surface_2) })

        val entries = data.recent
        if (entries.isNotEmpty()) {
            val maxCal = entries.maxOf { it.calories }.coerceAtLeast(1)
            val bars = entries.size
            val gapW = 18f
            val colWidth = ((miniRight - miniLeft) - (gapW * (bars - 1))) / bars
            entries.forEachIndexed { i, e ->
                val x = miniLeft + i * (colWidth + gapW)
                val h = (e.calories.toFloat() / maxCal) * (miniHeight - 24f)
                val rect = RectF(x, miniTop + miniHeight - h - 12f, x + colWidth, miniTop + miniHeight - 12f)
                // inactive fill first
                c.drawRoundRect(RectF(x, miniTop + 12f, x + colWidth, miniTop + miniHeight - 12f), 8f, 8f, barBg)
                // active bar on top
                c.drawRoundRect(rect, 8f, 8f, barFg)
            }
            c.drawText("Recent calories", miniLeft, miniTop - 8f, mutedPaint)
        }

        val card = viewHolder.view as ImageCardView
        card.mainImageView.setImageBitmap(bmp)

        // Accessibility description
        card.contentDescription = "Dashboard overview. Active ${data.activePercent} percent. Alerts ${data.alerts}. Energy ${data.energyPercent} percent."
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val card = viewHolder.view as ImageCardView
        card.mainImage = null
    }
}
