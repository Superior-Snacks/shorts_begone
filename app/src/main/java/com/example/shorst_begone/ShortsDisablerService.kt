package com.example.shorst_begone

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo


class ShortsDisablerService : AccessibilityService() {

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
        shortsNodes.clear()
    }

    private fun showOverlay(buttonBounds: Rect) {
        val overlayView = View(this)
        overlayView.setBackgroundColor(Color.TRANSPARENT) // Make it invisible

        overlayView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    Log.d("ShortsOverlay", "Blocked Shorts button tap")
                    view.performClick() // Call performClick() on ACTION_UP
                    true  // Consume the touch event
                }
                else -> true // Consume other touch events
            }
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

        val windowManager = getSystemService(WINDOW_SERVICE) as? WindowManager
        if (windowManager != null) {
            windowManager.addView(overlayView, overlayParams)
            Log.d("ShortsOverlay", "Overlay added at: (${buttonBounds.left}, ${buttonBounds.top})")
        } else {
            Log.e("ShortsOverlay", "WindowManager is null!")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Filter for window content changed events and check if the event is from the youtube app.
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && event.packageName == "com.google.android.youtube") {
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                disableShortsButton(rootNode)
            }
        }
    }

    override fun onInterrupt() {
        Log.d("ButtonDisabler", "Service interrupted")
    }
}