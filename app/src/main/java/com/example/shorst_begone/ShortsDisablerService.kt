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
        Log.d("node in question", "${shortsNodes}")

        // Iterate through the found nodes
        for (node in shortsNodes) {
            Log.d("start_check", "Started to check a found node")
            // Check if this is the correct node
            if (node.viewIdResourceName == "com.google.android.youtube:id/shorts_button") {
                // Get the grandparent
                val parent = node.parent
                val grandparent = parent?.parent
                val greatGrandparent = grandparent?.parent

                // Try to hide the great-grandparent
                if (greatGrandparent != null) {
                    try {
                        greatGrandparent.isVisibleToUser = false
                        Log.d("ButtonDisabler", "Shorts great-grandparent found and disabled.")
                    } catch (e: Exception) {
                        Log.e("ButtonDisabler", "Error disabling Shorts great-grandparent: ${e.message}")
                    }
                } else {
                    Log.w("ButtonDisabler", "Shorts great-grandparent not found.")
                }
                // Try to hide the grandparent
                if (grandparent != null) {
                    try {
                        grandparent.isVisibleToUser = false
                        Log.d("ButtonDisabler", "Shorts grandparent found and disabled.")
                    } catch (e: Exception) {
                        Log.e("ButtonDisabler", "Error disabling Shorts grandparent: ${e.message}")
                    }
                } else {
                    Log.w("ButtonDisabler", "Shorts grandparent not found.")
                }
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
            Log.d("NULL!!!!", "node was null you bitch")
        }
    }

    override fun onInterrupt() {
        Log.d("ButtonDisabler", "Service interrupted")
    }
}