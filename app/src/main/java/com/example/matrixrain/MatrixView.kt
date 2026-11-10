package com.example.matrixrain

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import androidx.core.graphics.ColorUtils
import kotlin.random.Random

/** Draws procedurally generated digital rain. Shared by the Activity and the DreamService. */
class MatrixView(context: Context) : View(context) {

    private val settings = Settings.load(context)

    private val rainColor = settings.color
    private val headColor = ColorUtils.blendARGB(settings.color, Color.WHITE, 0.8f)
    private val charSizePx = settings.sizeDp * resources.displayMetrics.density
    private val speedMul = settings.speedPct / 100f
    private val minSpeed = 6f * speedMul      // rows / second
    private val maxSpeed = 18f * speedMul
    private val maxTrail = settings.trail
    private val minTrail = (settings.trail * 0.3f).toInt().coerceAtLeast(3)

    private val glyphs =
        "ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ0123456789:.=*+-<>¦|"

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = charSizePx
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.CENTER
    }

    private var cols = 0
    private var rows = 0
    private lateinit var grid: CharArray
    private lateinit var head: FloatArray
    private lateinit var speed: FloatArray
    private lateinit var trail: IntArray

    private var lastNanos = 0L

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cols = (w / charSizePx).toInt().coerceAtLeast(1)
        rows = (h / charSizePx).toInt() + 2
        grid = CharArray(cols * rows) { randomGlyph() }
        head = FloatArray(cols)
        speed = FloatArray(cols)
        trail = IntArray(cols)
        for (c in 0 until cols) resetColumn(c, startAnywhere = true)
        lastNanos = 0L
    }

    private fun randomGlyph() = glyphs[Random.nextInt(glyphs.length)]

    private fun resetColumn(c: Int, startAnywhere: Boolean) {
        trail[c] = Random.nextInt(minTrail, maxTrail + 1)
        speed[c] = minSpeed + Random.nextFloat() * (maxSpeed - minSpeed)
        head[c] = if (startAnywhere) Random.nextFloat() * rows
        else -Random.nextFloat() * rows * 0.5f
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)
        if (cols == 0) return

        val now = System.nanoTime()
        val dt = if (lastNanos == 0L) 0f else ((now - lastNanos) / 1e9f).coerceAtMost(0.1f)
        lastNanos = now

        val cell = charSizePx
        val baseR = Color.red(rainColor)
        val baseG = Color.green(rainColor)
        val baseB = Color.blue(rainColor)

        for (c in 0 until cols) {
            head[c] += speed[c] * dt
            if (head[c] - trail[c] > rows) {
                resetColumn(c, startAnywhere = false)
                continue
            }
            val headRow = head[c].toInt()
            val x = c * cell + cell / 2f
            for (i in 0 until trail[c]) {
                val r = headRow - i
                if (r < 0 || r >= rows) continue
                val idx = c * rows + r
                if (Random.nextInt(60) == 0) grid[idx] = randomGlyph()

                if (i == 0) {
                    paint.color = headColor
                } else {
                    val a = (255 * (1f - i / trail[c].toFloat())).toInt().coerceIn(0, 255)
                    paint.color = Color.argb(a, baseR, baseG, baseB)
                }
                canvas.drawText(grid[idx].toString(), x, (r + 1) * cell, paint)
            }
        }
        postInvalidateOnAnimation()
    }

    override fun onDetachedFromWindow() {
        lastNanos = 0L
        super.onDetachedFromWindow()
    }
}
