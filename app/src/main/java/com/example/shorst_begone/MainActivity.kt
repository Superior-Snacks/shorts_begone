package com.example.shorst_begone


import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import com.example.shorst_begone.ui.theme.Shorst_begoneTheme

import android.accessibilityservice.AccessibilityService


class ShortsDisablerService : AccessibilityService() {

    private fun disableShortsButton(node: AccessibilityNodeInfo?) {
        if (node == null) return

        // Check if this node matches the Shorts button based on content description
        if (node.contentDescription?.toString() == "Shorts") {
            node.isClickable = false
            Log.d("ButtonDisabler", "Shorts button found and disabled.")
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