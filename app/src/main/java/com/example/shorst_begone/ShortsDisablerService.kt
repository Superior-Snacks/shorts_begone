package com.example.shorst_begone


import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.accessibilityservice.AccessibilityService
//import androidx.compose.ui.semantics.contentDescription


class ShortsDisablerService : AccessibilityService() {

    private fun disableShortsButton(rootNode: AccessibilityNodeInfo?) {
        if (rootNode == null) return

        // Find all nodes with text "Shorts"
        val shortsNodes = rootNode.findAccessibilityNodeInfosByText("Shorts")

        for (node in shortsNodes) {
            Log.d("NodeInfo", "contentDescription: ${node.contentDescription}, className: ${node.className}")

            //node.className?.contains("Button") == true
            val description = node.contentDescription?.trim()?.replace("\\s".toRegex(), "")
            val className = node.className?.trim()

            if (description != null && description.matches(Regex("(?i)^Shorts\$")) &&
                className != null && className.matches(Regex("(?i)^android\\.widget\\.Button(\\..*)?\$"))) {
                Log.d("ButtonDisabler", "Shorts button found and matched!")

                // Try to disable function
                node.isClickable = false  // Disable clicks
                node.isFocusable = false  // Prevent focus

                Log.d("ButtonDisabler??", "Shorts button disabled.")
            }
            else {
                Log.d("GayButtonDisabler", "NADA FOUND")
            }
        }
        shortsNodes.clear()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
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