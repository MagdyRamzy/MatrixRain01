package com.example.matrixrain

import android.content.Context
import android.graphics.Color

/** Saved settings. If nothing has been saved, the defaults below are used. */
object Settings {
    private const val PREFS = "matrix_prefs"

    val COLORS = listOf(
        "Green" to Color.rgb(0, 255, 70),
        "Red" to Color.rgb(255, 40, 40),
        "Blue" to Color.rgb(60, 120, 255),
        "Cyan" to Color.rgb(0, 230, 230),
        "Amber" to Color.rgb(255, 176, 0),
        "Purple" to Color.rgb(190, 90, 255),
        "White" to Color.rgb(240, 240, 240)
    )

    const val DEFAULT_SIZE_DP = 18      // character size
    const val DEFAULT_SPEED_PCT = 100   // speed, percent
    const val DEFAULT_TRAIL = 28        // longest trail, in characters

    data class Values(val color: Int, val sizeDp: Int, val speedPct: Int, val trail: Int)

    fun defaults() = Values(COLORS[0].second, DEFAULT_SIZE_DP, DEFAULT_SPEED_PCT, DEFAULT_TRAIL)

    fun load(context: Context): Values {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val d = defaults()
        return Values(
            p.getInt("color", d.color),
            p.getInt("size", d.sizeDp),
            p.getInt("speed", d.speedPct),
            p.getInt("trail", d.trail)
        )
    }

    fun save(context: Context, v: Values) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt("color", v.color)
            .putInt("size", v.sizeDp)
            .putInt("speed", v.speedPct)
            .putInt("trail", v.trail)
            .apply()
    }
}
