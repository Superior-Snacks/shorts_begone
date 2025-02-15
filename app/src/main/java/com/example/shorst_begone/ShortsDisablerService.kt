package com.example.shorst_begone


import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.accessibilityservice.AccessibilityService
//import androidx.compose.ui.semantics.contentDescription


class ShortsDisablerService : AccessibilityService() {

    private fun disableShortsButton(rootNode: AccessibilityNodeInfo?) {
        if (rootNode == null) return

        // Find all nodes with the text "Shorts"
        val shortsNodes = rootNode.findAccessibilityNodeInfosByText("Shorts")
        Log.d("node in question", "${shortsNodes}")

        // Iterate through the found nodes
        for (node in shortsNodes) {
            Log.d("NodeInfo", "contentDescription: ${node.contentDescription}, className: ${node.className}")
            Log.d("vis node", "Great-grandparent isVisibleToUser: ${node.parent?.parent?.isVisibleToUser}")
            // Check if this is the correct node
            if (node.contentDescription == "Shorts" && node.className == "android.widget.Button") {
                // Get the parent
                val parent = node.parent
                // Get the grandparent
                val grandparent = parent?.parent
                // Get the great-grandparent
                val greatGrandparent = grandparent?.parent

                // Try to hide the great-grandparent
                if (greatGrandparent != null) {
                    Log.d("Visibility", "Great-grandparent isVisibleToUser: ${greatGrandparent.isVisibleToUser}")
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
                    Log.d("Visibility", "Great-grandparent isVisibleToUser: ${grandparent.isVisibleToUser}")
                    try {
                        grandparent.isVisibleToUser = false
                        Log.d("ButtonDisabler", "Shorts grandparent found and disabled.")
                    } catch (e: Exception) {
                        Log.e("ButtonDisabler", "Error disabling Shorts grandparent: ${e.message}")
                    }
                } else {
                    Log.w("ButtonDisabler", "Shorts grandparent not found.")
                }
                // Try to hide the parent
                if (parent != null) {
                    try {
                        parent.isVisibleToUser = false
                        Log.d("ButtonDisabler", "Shorts parent found and disabled.")
                    } catch (e: Exception) {
                        Log.e("ButtonDisabler", "Error disabling Shorts parent: ${e.message}")
                    }
                } else {
                    Log.w("ButtonDisabler", "Shorts parent not found.")
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
        }
    }

    override fun onInterrupt() {
        Log.d("ButtonDisabler", "Service interrupted")
    }
}