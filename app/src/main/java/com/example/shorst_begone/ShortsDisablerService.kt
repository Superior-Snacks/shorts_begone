package com.example.shorst_begone


import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

import android.accessibilityservice.AccessibilityService
//import androidx.preference.forEach


class ShortsDisablerService : AccessibilityService() {

    private fun disableShortsButton(rootNode: AccessibilityNodeInfo?) {
        if (rootNode == null) return

        // Find all nodes with the text "Shorts"
        val shortsNodes = rootNode.findAccessibilityNodeInfosByText("Shorts")

        // Iterate through the found nodes
        for (node in shortsNodes) {
            // Check if this is the correct node
            if (node.viewIdResourceName == "com.google.android.youtube:id/shorts_button") {
                // Try to set the parent to invisible
                node.parent?.isVisibleToUser = false
                Log.d("ButtonDisabler", "Shorts button found and disabled.")
            }
        }
        shortsNodes.clear()
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