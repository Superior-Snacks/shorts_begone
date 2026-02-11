package com.example.shorst_begone

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.Calendar

class ShortsDisablerService : AccessibilityService() {

    // --- CONFIGURATION ---
    private val TEST_MODE = true  // <--- SET TO FALSE WHEN DONE TESTING
    // If TEST_MODE is true, it ignores the 10 PM check.

    private val INACTIVITY_TIMEOUT_MS = if (TEST_MODE) 10 * 1000L else 15 * 60 * 1000L
    private val START_HOUR = 22
    private val cooldown = 400L

    // --- VARIABLES ---
    private var lastBackTime: Long = 0
    private var isTimerRunning = false
    private var isWatchingNormalVideo = false

    // System Services
    private val timerHandler = Handler(Looper.getMainLooper())
    private var audioManager: AudioManager? = null

    // DEBUG: Track when the timer started
    private var timerStartTime: Long = 0

    // DEBUG: Ticker
    private val logTicker = object : Runnable {
        override fun run() {
            if (!isTimerRunning) return // Stop logging if timer stopped

            val elapsedMillis = System.currentTimeMillis() - timerStartTime
            val elapsedSeconds = elapsedMillis / 1000
            val remainingSeconds = (INACTIVITY_TIMEOUT_MS - elapsedMillis) / 1000

            Log.d("SleepTimerDebug", "Elapsed: ${elapsedSeconds}s | Remaining: ${remainingSeconds}s")
            timerHandler.postDelayed(this, 1000)
        }
    }

    // --- LIFECYCLE ---

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (Intent.ACTION_SCREEN_OFF == intent?.action) {
                stopSleepTimer()
            }
        }
    }

    private val sleepRunnable = Runnable {
        // Double check time (unless in test mode)
        if (TEST_MODE || isAfterBedtime()) {
            performSleepSequence()
        }
        stopSleepTimer() // Clean up flags after firing
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenStateReceiver, filter)

        Log.d("ShortsDisabler", "Service Ready - Test Mode: $TEST_MODE")
    }

    // --- MAIN LOGIC ---

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName != "com.google.android.youtube") return

        val rootNode = rootInActiveWindow ?: return

        // 1. CHECK FOR SHORTS
        if (isShortsView(rootNode)) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackTime > cooldown) {
                performGlobalAction(GLOBAL_ACTION_BACK)
                Log.d("ShortsDisabler", "Shorts detected -> GOING BACK")
                lastBackTime = currentTime
                stopSleepTimer()
            }
            return
        }

        // 2. CHECK FOR NORMAL VIDEO
        if (isNormalVideoView(rootNode)) {
            isWatchingNormalVideo = true

            if (isUserInteraction(event)) {
                // User clicked/scrolled -> RESET the timer back to 0
                resetSleepTimer()
            } else {
                // Passive event (time update) -> Ensure timer is ON, but DO NOT RESET IT
                ensureTimerRunning()
            }
        } else {
            // 3. BROWSING
            isWatchingNormalVideo = false
            stopSleepTimer()
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (isWatchingNormalVideo && event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.KEYCODE_VOLUME_DOWN,
                KeyEvent.KEYCODE_VOLUME_MUTE -> {
                    resetSleepTimer()
                    return false
                }
            }
        }
        return super.onKeyEvent(event)
    }

    // --- VIEW DETECTION ---

    private fun isShortsView(rootNode: AccessibilityNodeInfo): Boolean {
        return rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_recycler").isNotEmpty()
    }

    private fun isNormalVideoView(rootNode: AccessibilityNodeInfo): Boolean {
        val playerNodes = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/player_view")
        val watchNodes = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/watch_player")
        return playerNodes.isNotEmpty() || watchNodes.isNotEmpty()
    }

    private fun isUserInteraction(event: AccessibilityEvent): Boolean {
        return event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED
    }

    // --- TIMER HELPERS ---

    private fun ensureTimerRunning() {
        // Only start if it is NOT currently running
        if (!isTimerRunning) {
            startTimerInternal()
        }
    }

    private fun resetSleepTimer() {
        // Stop current, then start new
        stopSleepTimer()
        startTimerInternal()
    }

    private fun startTimerInternal() {
        if (TEST_MODE || isAfterBedtime()) {
            isTimerRunning = true
            timerStartTime = System.currentTimeMillis()

            // Post the actual task
            timerHandler.postDelayed(sleepRunnable, INACTIVITY_TIMEOUT_MS)

            // Post the debug logger
            timerHandler.post(logTicker)

            Log.d("SleepTimer", "Timer STARTED")
        }
    }

    private fun stopSleepTimer() {
        if (isTimerRunning) {
            isTimerRunning = false
            timerHandler.removeCallbacks(sleepRunnable)
            timerHandler.removeCallbacks(logTicker)
            Log.d("SleepTimer", "Timer STOPPED")
        }
    }

    private fun isAfterBedtime(): Boolean {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return currentHour >= START_HOUR || currentHour < 5
    }

    private fun performSleepSequence() {
        Log.d("SleepTimer", "Bedtime triggered.")

        // FIX: Check if audio is actually playing!
        // If isMusicActive is false, the video is already paused, so we skip the button press.
        if (audioManager?.isMusicActive == true) {
            val eventTime = System.currentTimeMillis()

            // FIX: Use KEYCODE_MEDIA_PAUSE (127) instead of PLAY_PAUSE (85)
            // This tells Android "Pause strictly", not "Toggle".
            val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE, 0)
            val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE, 0)

            audioManager?.dispatchMediaKeyEvent(downEvent)
            audioManager?.dispatchMediaKeyEvent(upEvent)
            Log.d("SleepTimer", "Music was active -> Paused.")
        } else {
            Log.d("SleepTimer", "Music already paused -> Skipping pause command.")
        }

        // Lock Screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenStateReceiver)
        stopSleepTimer()
    }

    override fun onInterrupt() {}
}