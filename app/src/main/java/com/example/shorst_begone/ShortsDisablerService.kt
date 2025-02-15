package com.example.shorst_begone


import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.accessibilityservice.AccessibilityService
//import androidx.compose.ui.semantics.contentDescription


private fun disableShortsButton(rootNode: AccessibilityNodeInfo?) {
    if (rootNode == null) return

    val shortsNodes = rootNode.findAccessibilityNodeInfosByText("Shorts")

    for (node in shortsNodes) {
        if (node.contentDescription?.trim() == "Shorts" && node.className?.contains("Button") == true) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            Log.d("ShortsOverlay", "Button found at: $rect")

            if (!rect.isEmpty) {
                showOverlay(rect)  // Create an overlay at the Shorts button location
            }
        }
    }
}