package com.example.shorst_begone

import android.accessibilityservice.AccessibilityService
//import android.graphics.Color
//import android.graphics.PixelFormat
//import android.graphics.Rect
import android.util.Log
//import android.view.MotionEvent
//import android.view.View
//import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo


class ShortsDisablerService : AccessibilityService() {



    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.packageName == "com.google.android.youtube") {

            val rootNode = rootInActiveWindow
            rootNode?.let { disableShortsButton(it) }
        }
    }

    private fun isShortsView(rootNode: AccessibilityNodeInfo?): Boolean {
        if (rootNode == null) return false

        // Find the Shorts feed (RecyclerView) by its ID
        val shortsFeedNodes = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_recycler")

        if (shortsFeedNodes.isNotEmpty()) {
            Log.d("ShortsDetection", "Shorts view detected!")
            return true
        }

        return false
    }

    override fun onInterrupt() {
        Log.d("ButtonDisabler", "Service interrupted")
    }
}