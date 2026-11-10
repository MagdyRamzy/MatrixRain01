package com.example.matrixrain

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import kotlin.math.roundToInt

/** Opened by long-pressing the rain. Changes are saved when you leave this screen. */
class SettingsActivity : Activity() {

    private var values = Settings.defaults()
    private val hsv = FloatArray(3)

    private lateinit var preview: View
    private lateinit var hexText: TextView
    private lateinit var hueBar: SeekBar
    private lateinit var satBar: SeekBar
    private lateinit var valBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Matrix Rain settings"
        values = Settings.load(this)
        build()
    }

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    private fun build() {
        Color.colorToHSV(values.color, hsv)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(24))
        }

        // ---- Color ----
        root.addView(label("Color"))

        preview = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56))
        }
        root.addView(preview)

        hexText = TextView(this).apply {
            textSize = 14f
            setPadding(0, dp(4), 0, dp(4))
        }
        root.addView(hexText)

        hueBar = addSeek(root, "Hue", 0, 360, hsv[0].roundToInt(), "°") {
            hsv[0] = it.toFloat(); applyColor()
        }
        satBar = addSeek(root, "Saturation", 0, 100, (hsv[1] * 100).roundToInt(), "%") {
            hsv[1] = it / 100f; applyColor()
        }
        valBar = addSeek(root, "Brightness", 5, 100, (hsv[2] * 100).roundToInt(), "%") {
            hsv[2] = it / 100f; applyColor()
        }

        // Quick presets (optional shortcuts that move the sliders)
        root.addView(label("Quick colors"))
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        Settings.COLORS.forEach { (_, c) ->
            row.addView(View(this).apply {
                setBackgroundColor(c)
                layoutParams = LinearLayout.LayoutParams(0, dp(36), 1f).apply {
                    setMargins(dp(2), 0, dp(2), 0)
                }
                setOnClickListener {
                    Color.colorToHSV(c, hsv)
                    hueBar.progress = hsv[0].roundToInt()
                    satBar.progress = (hsv[1] * 100).roundToInt()
                    valBar.progress = (hsv[2] * 100).roundToInt() - 5
                    applyColor()
                }
            })
        }
        root.addView(row)
        applyColor()

        // ---- Other sliders ----
        addSeek(root, "Character size", 10, 40, values.sizeDp, " dp") { values = values.copy(sizeDp = it) }
        addSeek(root, "Speed", 25, 300, values.speedPct, "%") { values = values.copy(speedPct = it) }
        addSeek(root, "Trail length", 6, 40, values.trail, " chars") { values = values.copy(trail = it) }

        // ---- Reset ----
        root.addView(Button(this).apply {
            text = "Reset to defaults"
            setOnClickListener {
                values = Settings.defaults()
                Settings.save(this@SettingsActivity, values)
                recreate()
            }
        })

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun applyColor() {
        hsv[1] = hsv[1].coerceIn(0f, 1f)
        hsv[2] = hsv[2].coerceIn(0.05f, 1f)
        val c = Color.HSVToColor(hsv)
        values = values.copy(color = c)
        preview.setBackgroundColor(c)
        hexText.text = String.format("#%06X", 0xFFFFFF and c)
    }

    private fun label(text: String) = TextView(this).apply {
        this.text = text
        textSize = 16f
        setPadding(0, dp(16), 0, dp(4))
    }

    private fun addSeek(
        parent: LinearLayout, name: String, min: Int, max: Int, current: Int,
        unit: String, onChange: (Int) -> Unit
    ): SeekBar {
        val title = label("$name: $current$unit")
        val bar = SeekBar(this).apply {
            this.max = max - min
            progress = current - min
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) {
                    val v = p + min
                    title.text = "$name: $v$unit"
                    if (fromUser) onChange(v)
                }
                override fun onStartTrackingTouch(s: SeekBar) {}
                override fun onStopTrackingTouch(s: SeekBar) {}
            })
        }
        parent.addView(title)
        parent.addView(bar)
        return bar
    }

    override fun onPause() {
        Settings.save(this, values)
        super.onPause()
    }
}
