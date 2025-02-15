package com.example.shorst_begone


import android.util.Log
//import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
//import android.accessibilityservice.AccessibilityService
//import androidx.compose.ui.semantics.contentDescription
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
//import android.util.Log
import android.view.View
import android.view.WindowManager
//import android.view.accessibility.AccessibilityEvent
//import android.view.accessibility.AccessibilityNodeInfo


private fun disableShortsButton(rootNode: AccessibilityNodeInfo?) {
    if (rootNode == null) return

    val shortsNodes = rootNode.findAccessibilityNodeInfosByText("Shorts")

    for (node in shortsNodes) {
        if (node.contentDescription?.trim() == "Shorts" && node.className?.contains("Button") == true) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            Log.d("ShortsOverlay", "Button found at: $rect")

            if (!rect.isEmpty) {
                showOverlay(rect)  // Create an overlay at the Shorts button location
            }
        }
    }
}

private fun showOverlay(buttonBounds: Rect) {
    val overlayView = View(this)
    overlayView.setBackgroundColor(Color.TRANSPARENT) // Make it invisible

    overlayView.setOnTouchListener { _, _ ->
        // Block touch events so Shorts cannot be tapped
        Log.d("ShortsOverlay", "Blocked Shorts button tap")
        true
    }

    val overlayParams = WindowManager.LayoutParams(
        buttonBounds.width(), // Match button width
        buttonBounds.height(), // Match button height
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    )

    overlayParams.x = buttonBounds.left
    overlayParams.y = buttonBounds.top

    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowManager.addView(overlayView, overlayParams)

    Log.d("ShortsOverlay", "Overlay added at: (${buttonBounds.left}, ${buttonBounds.top})")
}