package com.example.shorst_begone

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo


class ShortsDisablerService : AccessibilityService() {


    private var last_back: Long = 0
    private val cooldown = 40
    //when the youtube app changes view if it is a short then go back
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d("START", "began")
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.packageName == "com.google.android.youtube") {

            val rootNode = rootInActiveWindow
            if (isShortsView(rootNode)) {
                val current_time = System.currentTimeMillis()

                if (current_time - last_back > cooldown) {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    Log.d("BACK", "Shorts detected, shit!! go back")
                    last_back = current_time
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