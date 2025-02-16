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

    private fun disableShortsButton(rootNode: AccessibilityNodeInfo?) {
        if (rootNode == null) return

        // Find all nodes with text "Shorts"
        val shortsNodes = rootNode.findAccessibilityNodeInfosByText("Shorts")
        val homeNodes = rootNode.findAccessibilityNodeInfosByText("Home")

        for (node in shortsNodes) {
            Log.d("NodeInfo", "contentDescription: ${node.contentDescription}, className: ${node.className}")

            val description = node.contentDescription?.trim()?.replace("\\s".toRegex(), "")
            val className = node.className?.trim()

            if (description != null && description.matches(Regex("(?i)^Shorts\$")) &&
                className != null && className.matches(Regex("(?i)^android\\.widget\\.Button(\\..*)?\$"))) {
                Log.d("ButtonDisabler", "Shorts button found and matched!")


                // Try to remove/hide the button
                if (node.isClickable) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK) // Click to navigate away
                } else {
                    node.performAction(AccessibilityNodeInfo.ACTION_DISMISS) // Try dismissing
                }


                Log.d("ButtonDisabler", "Shorts button disabled.")
            }
        }
        shortsNodes.clear()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.packageName == "com.google.android.youtube") {

            val rootNode = rootInActiveWindow
            rootNode?.let { disableShortsButton(it) }
        }
    }

    override fun onInterrupt() {
        Log.d("ButtonDisabler", "Service interrupted")
    }
}