package dk.babyapp.tracking

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.SystemClock
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import dk.babyapp.MainActivity
import dk.babyapp.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs

/** Whether the app is on screen; the floating timer is only shown while it is not. */
object AppVisibility {
    private val visibleCount = MutableStateFlow(0)
    val visible: StateFlow<Int> get() = visibleCount
    fun started() { visibleCount.value += 1 }
    fun stopped() { visibleCount.value = (visibleCount.value - 1).coerceAtLeast(0) }
}

/** What the floating timer shows. */
data class OverlayTimer(val label: String, val elapsedSeconds: Long, val running: Boolean, val accent: Int)

/**
 * A small floating timer over other apps, like the Samsung clock. It needs the "Vis over andre apps" permission,
 * which is the opt-in. Drag to move, tap to open the app, × to hide it without stopping the timer.
 */
class TimerOverlay(private val context: Context, private val onClose: () -> Unit) {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private var root: LinearLayout? = null
    private lateinit var title: TextView
    private lateinit var clock: Chronometer
    private lateinit var dot: View
    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
    ).apply { gravity = Gravity.TOP or Gravity.END; x = dp(12); y = dp(140) }

    fun show(timer: OverlayTimer) {
        if (!Settings.canDrawOverlays(context)) { hide(); return }
        val view = root ?: build().also { root = it; runCatching { windowManager.addView(it, params) }.onFailure { root = null; return } }
        title.text = timer.label
        (dot.background as GradientDrawable).setColor(timer.accent)
        clock.base = SystemClock.elapsedRealtime() - timer.elapsedSeconds * 1_000
        if (timer.running) clock.start() else clock.stop()
        clock.alpha = if (timer.running) 1f else 0.55f
        view.contentDescription = "${timer.label}, tryk for at åbne appen"
    }

    fun hide() {
        root?.let { view -> clock.stop(); runCatching { windowManager.removeView(view) } }
        root = null
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun build(): LinearLayout {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(8), dp(6), dp(8))
            background = GradientDrawable().apply { cornerRadius = dp(24).toFloat(); setColor(Color.WHITE); setStroke(dp(1), Color.parseColor("#D6D3DC")) }
            elevation = dp(6).toFloat()
        }
        dot = View(context).apply { background = GradientDrawable().apply { shape = GradientDrawable.OVAL } }
        layout.addView(dot, LinearLayout.LayoutParams(dp(10), dp(10)).apply { marginEnd = dp(8) })
        title = TextView(context).apply { setTextColor(Color.parseColor("#1C1B1F")); setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f) }
        layout.addView(title, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(8) })
        clock = Chronometer(context).apply { setTextColor(Color.parseColor("#1C1B1F")); setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f); format = "%s" }
        layout.addView(clock)
        val close = ImageView(context).apply {
            setImageResource(R.drawable.ic_overlay_close)
            setPadding(dp(8), dp(4), dp(4), dp(4))
            contentDescription = "Skjul den flydende tæller (tælleren kører videre)"
            setOnClickListener { hide(); onClose() }
        }
        layout.addView(close, LinearLayout.LayoutParams(dp(32), dp(28)))
        // Drag to move; a touch that hardly moves is a tap and opens the app.
        var downX = 0f; var downY = 0f; var startX = 0; var startY = 0
        layout.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { downX = event.rawX; downY = event.rawY; startX = params.x; startY = params.y; true }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX - (event.rawX - downX).toInt(); params.y = startY + (event.rawY - downY).toInt()
                    runCatching { windowManager.updateViewLayout(view, params) }; true
                }
                MotionEvent.ACTION_UP -> {
                    if (abs(event.rawX - downX) < dp(8) && abs(event.rawY - downY) < dp(8)) {
                        view.performClick()
                        context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    }
                    true
                }
                else -> false
            }
        }
        return layout
    }

    private fun dp(value: Int) = (value * context.resources.displayMetrics.density).toInt()
}
