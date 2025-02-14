package com.example.shorst_begone


import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

import android.accessibilityservice.AccessibilityService


class ShortsDisablerService : AccessibilityService() {

    private fun disableShortsButton(node: AccessibilityNodeInfo?) {
        if (node == null) return

        // Check if this node matches the Shorts button based on content description
        if (node.contentDescription?.toString() == "Shorts") {
            node.isClickable = false
            Log.d("ButtonDisabler", "Shorts button found and disabled.")
        }
        else {
            Log.d("ButtonDisabler", "Shorts button not found.")
        }

        // Traverse child nodes recursively
        for (i in 0 until node.childCount) {
            disableShortsButton(node.getChild(i))
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