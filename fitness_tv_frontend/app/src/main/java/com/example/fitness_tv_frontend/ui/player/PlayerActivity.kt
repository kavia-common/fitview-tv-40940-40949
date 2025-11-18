package com.example.fitness_tv_frontend.ui.player

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.fitness_tv_frontend.R
import com.example.fitness_tv_frontend.model.Workout
import kotlin.math.roundToInt

/**
 * PlayerActivity plays a sample workout (HLS/DASH or local).
 * Displays overlay with title, elapsed time, and estimated calories.
 */
class PlayerActivity : FragmentActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var overlay: View
    private lateinit var titleTv: TextView
    private lateinit var timeTv: TextView
    private lateinit var caloriesTv: TextView

    private var workout: Workout? = null
    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            updateOverlay()
            handler.postDelayed(this, 500)
        }
    }

    companion object {
        const val EXTRA_WORKOUT = "extra_workout"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        workout = intent.getParcelableExtra(EXTRA_WORKOUT)
        playerView = findViewById(R.id.player_view)
        overlay = findViewById(R.id.overlay)
        titleTv = findViewById(R.id.overlay_title)
        timeTv = findViewById(R.id.overlay_time)
        caloriesTv = findViewById(R.id.overlay_calories)

        titleTv.text = workout?.title ?: getString(R.string.app_name)
    }

    private fun initPlayer() {
        player = ExoPlayer.Builder(this).build().also { p ->
            playerView.player = p
            val url = workout?.videoUrl
            val mediaItem = MediaItem.fromUri(url ?: "asset:///sample.mp4")
            p.setMediaItem(mediaItem)
            p.prepare()
            p.playWhenReady = true
        }
        handler.post(ticker)
    }

    @SuppressLint("SetTextI18n")
    private fun updateOverlay() {
        val p = player ?: return
        val pos = p.currentPosition.coerceAtLeast(0L)
        val seconds = (pos / 1000).toInt()
        val min = seconds / 60
        val sec = seconds % 60
        timeTv.text = String.format("%02d:%02d", min, sec)

        // Calorie estimate: keep in healthy range and present as integer kcal
        val totalCal = (workout?.caloriesEstimate ?: 100).coerceIn(20, 1200)
        val durationSec = ((workout?.durationMin ?: 15) * 60).coerceAtLeast(1)
        val est = ((seconds.toFloat() / durationSec) * totalCal).coerceIn(0f, totalCal.toFloat()).roundToInt()
        caloriesTv.text = "$est kcal"
        overlay.contentDescription = "Time ${timeTv.text}, calories $est kilocalories"
    }

    override fun onStart() {
        super.onStart()
        initPlayer()
    }

    override fun onResume() {
        super.onResume()
        // Resume ticker if returning from background
        handler.removeCallbacks(ticker)
        handler.post(ticker)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ticker)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(ticker)
        playerView.player = null
        player?.release()
        player = null
    }
}
