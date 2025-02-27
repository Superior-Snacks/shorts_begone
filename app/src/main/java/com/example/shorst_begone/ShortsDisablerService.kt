package com.example.shorst_begone

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo


class ShortsDisablerService : AccessibilityService() {
    private var last_back: Long = 0
    private val cooldown = 40 // I cannot make the app crash
    //when the youtube app changes view if it is a short then go back
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.packageName == "com.google.android.youtube") {

            val rootNode = rootInActiveWindow
            if (isShortsView(rootNode)) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - last_back > cooldown) {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    Log.d("BACK", "Shorts detected, mr.President get down!")
                    last_back = currentTime
                }
                else {
                    Log.d("SKIP", "Shorts detected, WE ARE WORKING ON IT")

                }
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