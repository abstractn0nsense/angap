package com.angap.photosendguide

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class GuideOverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var currentStepIndex = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf(startId)
        } else if (overlayView == null) {
            showOverlay()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        overlayView?.let { view -> runCatching { windowManager.removeView(view) } }
        overlayView = null
        super.onDestroy()
    }

    private fun showOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val content = createOverlayContent()
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM
        }

        try {
            overlayView = content
            windowManager.addView(content, layoutParams)
            updateContent()
        } catch (_: SecurityException) {
            overlayView = null
            stopSelf()
        }
    }

    private fun createOverlayContent(): View {
        val padding = 24.dp
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadii = floatArrayOf(32f, 32f, 32f, 32f, 0f, 0f, 0f, 0f)
            }

            val progress = TextView(context).apply {
                tag = "progress"
                setTextColor(Color.DKGRAY)
                textSize = 20f
            }
            val title = TextView(context).apply {
                tag = "title"
                setTextColor(Color.BLACK)
                textSize = 28f
                setPadding(0, 12.dp, 0, 8.dp)
            }
            val description = TextView(context).apply {
                tag = "description"
                setTextColor(Color.DKGRAY)
                textSize = 22f
            }
            val buttons = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
                setPadding(0, 20.dp, 0, 0)
            }
            val previous = Button(context).apply {
                text = "이전"
                minimumHeight = 56.dp
                setOnClickListener {
                    if (currentStepIndex > 0) {
                        currentStepIndex--
                        updateContent()
                    }
                }
            }
            val next = Button(context).apply {
                tag = "next"
                minimumHeight = 56.dp
                setOnClickListener {
                    if (currentStepIndex == guideSteps.lastIndex) stopSelf() else {
                        currentStepIndex++
                        updateContent()
                    }
                }
            }
            val close = Button(context).apply {
                text = "닫기"
                minimumHeight = 56.dp
                setOnClickListener { stopSelf() }
            }

            buttons.addView(previous)
            buttons.addView(next)
            buttons.addView(close)
            addView(progress)
            addView(title)
            addView(description)
            addView(buttons)
            updateContent()
        }
    }

    private fun updateContent() {
        val content = overlayView as? LinearLayout ?: return
        val step = guideSteps[currentStepIndex]
        content.findViewWithTag<TextView>("progress").text = "${currentStepIndex + 1} / ${guideSteps.size} 단계"
        content.findViewWithTag<TextView>("title").text = step.title
        content.findViewWithTag<TextView>("description").text = step.description
        content.findViewWithTag<Button>("next").text = if (currentStepIndex == guideSteps.lastIndex) "마치기" else "다음"
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
