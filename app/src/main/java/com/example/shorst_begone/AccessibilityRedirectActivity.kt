package com.example.shorst_begone

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings

class AccessibilityRedirectActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create an Intent to open the Accessibility Settings
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

        // Add these flags to clear the task and start a new one
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Start the Accessibility Settings Activity
        startActivity(intent)

        // Finish this Activity so the user doesn't come back to it
        finish()
    }
}