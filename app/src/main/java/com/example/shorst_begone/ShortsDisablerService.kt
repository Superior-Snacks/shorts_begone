package com.example.shorst_begone

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.geometry.isEmpty


class ShortsDisablerService : AccessibilityService() {
    private var last_back: Long = 0
    private val cooldown = 1000 // Increased cooldown
    private val maxRetries = 3
    private val retryDelay = 200L
    private var retries = 0

    //when the youtube app changes view if it is a short then go back
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d("ShortsGoBacker", "onAccessibilityEvent called")
        Log.d("ShortsGoBacker", "Event type: ${event.eventType}")
        Log.d("ShortsGoBacker", "Package name: ${event.packageName}")

        if (event.packageName == "com.google.android.youtube") {
            Log.d("ShortsGoBacker", "YouTube app event detected")
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                Log.d("ShortsGoBacker", "TYPE_WINDOW_CONTENT_CHANGED event")
                handleYouTubeEvent()
            } else if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                Log.d("ShortsGoBacker", "TYPE_WINDOW_STATE_CHANGED event")
                handleYouTubeEvent()
            }
        }
    }

    private fun handleYouTubeEvent() {
        Log.d("ShortsGoBacker", "handleYouTubeEvent called")
        retries = 0
        checkRootNode()
    }

    private fun checkRootNode() {
        Log.d("ShortsGoBacker", "checkRootNode called, retries: $retries")
        Handler(Looper.getMainLooper()).postDelayed({
            val rootNode = rootInActiveWindow
            Log.d("ShortsGoBacker", "rootNode: $rootNode")

            if (rootNode == null) {
                if (retries < maxRetries) {
                    retries++
                    Log.d("ShortsGoBacker", "rootNode is null, retrying in $retryDelay ms, retries: $retries")
                    checkRootNode()
                } else {
                    Log.d("ShortsGoBacker", "rootNode is null, max retries reached")
                }
            } else {
                if (isShortsView(rootNode)) {
                    val currentTime = System.currentTimeMillis()
                    Log.d("ShortsGoBacker", "currentTime: $currentTime")
                    Log.d("ShortsGoBacker", "last_back: $last_back")

                    if (currentTime - last_back > cooldown) {
                        performGlobalAction(GLOBAL_ACTION_BACK)
                        Log.d("ShortsGoBacker", "Shorts detected, mr.President get down!")
                        last_back = currentTime
                    } else {
                        Log.d("ShortsGoBacker", "Shorts detected, WE ARE WORKING ON IT")
                    }
                }
            }
        }, 1500) // 1500 milliseconds delay
    }

    private fun isShortsView(rootNode: AccessibilityNodeInfo?): Boolean {
        if (rootNode == null) {
            Log.d("ShortsGoBacker", "isShortsView: rootNode is null")
            return false
        }
        // Find the Shorts feed
        val shortsFeedNodes = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_recycler")
        Log.d("ShortsGoBacker", "shortsFeedNodes: $shortsFeedNodes")

        if (shortsFeedNodes.isEmpty()) {
            Log.d("ShortsGoBacker", "isShortsView: shortsFeedNodes is empty")
            return false
        }

        if (shortsFeedNodes.isNotEmpty()) {
            Log.d("ShortsGoBacker", "Shorts view detected!")
            return true
        }
        return false
    }

    override fun onInterrupt() {
        Log.d("ShortsGoBacker", "Service interrupted")
    }
}