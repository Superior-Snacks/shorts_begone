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

            if (node.contentDescription == "Shorts" && node.className == "android.widget.Button") {
                Log.d("ButtonDisabler", "Shorts button found")

                // Try to remove/hide the button
                if (node.isClickable) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK) // Click to navigate away
                } else {
                    node.performAction(AccessibilityNodeInfo.ACTION_DISMISS) // Try dismissing
                }

                // Try removing from parent (optional)
                //val parent = node.parent
                //parent?.removeChild(node)

                Log.d("ButtonDisabler??", "Shorts button disabled.")
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