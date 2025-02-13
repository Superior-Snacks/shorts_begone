package com.example.shorst_begone

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;


private void disableShortsButton(AccessibilityNodeInfo node) {
    if (node == null) return;

    // Check if this node matches the Shorts button based on content description or text
    if (node.getContentDescription() != null && node.getContentDescription().toString().equals("Shorts")) {
        node.setClickable(false);  // Attempt to disable clickability
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, null);  // Optional: Disable further interactions
        Log.d("ButtonDisabler", "Shorts button found and disabled.");
    }

    // Traverse child nodes recursively
    for (int i = 0; i < node.getChildCount(); i++) {
        disableShortsButton(node.getChild(i));
    }
}

@Override
public void onAccessibilityEvent(AccessibilityEvent event) {
    if (event.getSource() == null) return;
    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
    if (rootNode != null) {
        disableShortsButton(rootNode);
    }
}