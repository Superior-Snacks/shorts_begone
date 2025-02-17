package com.example.shorst_begone

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo


class ShortsDisablerService : AccessibilityService() {


    private var last_back = 0
    //when the youtube app changes view if it is a short then go back
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d("START", "began")
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.packageName == "com.google.android.youtube") {

            val rootNode = rootInActiveWindow
            if (isShortsView(rootNode)) {
                performGlobalAction(GLOBAL_ACTION_BACK)
                Log.d("BACK", "Shorts detected, shit!! go back")
            }
        }
    }

    private fun isShortsView(rootNode: AccessibilityNodeInfo?): Boolean {
        if (rootNode == null) return false

        // Find the Shorts feed
        val shortsFeedNodes = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_recycler")

        if (shortsFeedNodes.isNotEmpty()) {
            Log.d("ShortsDetection", "Shorts view detected!")
            return true
        }

        return false
    }

    override fun onInterrupt() {
        Log.d("ShortsGoBacker", "Service interrupted")
    }
}