package com.example.matrixrain

import android.service.dreams.DreamService

/** System screensaver (Daydream). Runs the rain with defaults; no config screen. */
class MatrixDream : DreamService() {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false   // any touch wakes the device
        isFullscreen = true
        isScreenBright = true
        setContentView(MatrixView(this))
    }
}
